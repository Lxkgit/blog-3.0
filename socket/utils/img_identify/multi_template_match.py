import os
import shutil
import cv2
import numpy as np
import re
import random
import string

from PIL import Image

from tensorflow.keras.applications import EfficientNetB0
from tensorflow.keras.applications.efficientnet import preprocess_input
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.models import Model

# 路径设置
BASE_DIR = r"D:\data\data\face"
# BASE_DIR = os.path.dirname(os.path.abspath(__file__))

TEMPLATE_DIR = os.path.join(BASE_DIR, "template")
TARGET_DIR = os.path.join(BASE_DIR, "target")
RESULT_DIR = os.path.join(TARGET_DIR, "result")
RESULT_DRAW_DIR = os.path.join(RESULT_DIR, "draw")
RESULT_SOURCE_DIR = os.path.join(RESULT_DIR, "source")

os.makedirs(RESULT_DIR, exist_ok=True)
os.makedirs(RESULT_DRAW_DIR, exist_ok=True)
os.makedirs(RESULT_SOURCE_DIR, exist_ok=True)

# EfficientNet 参数
IMG_SIZE = (224, 224)

# OpenCV 多尺度模板匹配参数
# 模板最小缩放比例
SCALE_MIN = 0.5
# 模板最大缩放比例
SCALE_MAX = 1.5
# 每次缩放步长
SCALE_STEP = 0.05

# ============================================================
# OpenCV 与 EfficientNet 综合评分权重
#
# TEMPLATE_WEIGHT：
#   OpenCV 模板匹配分数的权重。
#   主要反映：
#   - 模板与目标区域的像素结构相似程度
#   - 轮廓、纹理、局部形状等低层视觉信息
#   - 对位置和尺寸变化比较敏感
#
# FEATURE_WEIGHT：
#   EfficientNet 特征相似度的权重。
#   主要反映：
#   - 模板与目标区域经过神经网络提取后的视觉特征相似程度
#   - 对整体形状、纹理、语义视觉特征更加敏感
#   - 相比单纯像素匹配，对一定程度的变化更加鲁棒
#
# 最终综合分数：
#   final_score = template_score * TEMPLATE_WEIGHT + feature_score * FEATURE_WEIGHT
# 注意：
#   这里的权重不是“识别准确率”或“概率”。
#   它们只是控制两种相似度对最终评分的影响程度。
#
# ============================================================

TEMPLATE_WEIGHT = 0.3
FEATURE_WEIGHT = 0.7

# EfficientNet-B0 特征提取模型
def load_feature_model():
    print("正在加载 EfficientNet-B0...")
    base_model = EfficientNetB0(weights="imagenet", include_top=False, pooling="avg")
    model = Model(inputs=base_model.input, outputs=base_model.output)
    print("[OK] EfficientNet-B0 加载完成")
    return model


# EfficientNet 图片预处理
def load_and_preprocess(path):
    img = (Image.open(path).convert("RGB").resize(IMG_SIZE))
    img = img_to_array(img)
    img = np.expand_dims(img, axis=0)
    img = preprocess_input(img)
    return img


# EfficientNet 提取图片特征
def extract_feature_from_image(image):
    # OpenCV 是 BGR
    # EfficientNet 使用 RGB
    if isinstance(image, np.ndarray):
        if image is None:
            raise ValueError("图片数据为空")
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        image = Image.fromarray(image)
    # PIL Image
    elif isinstance(image, Image.Image):
        image = image.convert("RGB")
    else:
        raise TypeError("image 必须是 OpenCV numpy.ndarray 或者 PIL.Image.Image")
    # 调整尺寸
    image = image.resize(IMG_SIZE)
    # 转 numpy
    image = img_to_array(image)
    image = np.expand_dims(image, axis=0)
    # EfficientNet 预处理
    image = preprocess_input(image)
    # 提取特征
    feature = feature_model.predict(image, verbose=0)[0]
    # L2 归一化
    norm = np.linalg.norm(feature)
    if norm != 0:
        feature = (feature / norm)
    return feature


# 从文件提取 EfficientNet 特征
def extract_feature(path):
    image = cv2.imread(path, cv2.IMREAD_COLOR)
    if image is None:
        raise ValueError(f"无法读取图片: {path}")
    return extract_feature_from_image(image)


# 计算两个特征向量的余弦相似度
def calculate_feature_similarity(feature1, feature2):
    if feature1 is None:
        return 0.0
    if feature2 is None:
        return 0.0
    similarity = np.dot(feature1, feature2)
    # 理论上 L2 归一化之后范围就是 -1 ~ 1
    # 为了后续综合评分方便，限制一下范围。
    similarity = float(np.clip(similarity, -1.0, 1.0))
    return similarity


# 从模板匹配结果中获取 Top N 候选
def get_top_candidates(result, top_n=5, min_distance=30):
    candidates = []
    result_copy = result.copy()
    for _ in range(top_n):
        # 当前最高点
        _, max_val, _, max_loc = (cv2.minMaxLoc(result_copy))
        if max_val < -1.0:
            break
        x = int(max_loc[0])
        y = int(max_loc[1])
        score = float(max_val)
        # 保存候选
        candidates.append({"score": score, "x": x, "y": y})
        # 抑制当前点附近区域
        x1 = max(0, x - min_distance)
        y1 = max(0, y - min_distance)
        x2 = min(result_copy.shape[1], x + min_distance + 1)
        y2 = min(result_copy.shape[0], y + min_distance + 1)
        result_copy[y1:y2, x1:x2] = -1.0
    return candidates


# 加载模板图片路径
def load_template_paths():
    db = {}
    if not os.path.exists(TEMPLATE_DIR):
        print(f"[ERROR] 模板目录不存在: {TEMPLATE_DIR}")
        return db
    for cls in os.listdir(TEMPLATE_DIR):
        cls_dir = os.path.join(TEMPLATE_DIR, cls)
        # 只处理目录
        if not os.path.isdir(cls_dir):
            continue
        paths = []
        for img_name in os.listdir(cls_dir):
            path = os.path.join(cls_dir, img_name)
            # 只处理文件
            if not os.path.isfile(path):
                continue
            ext = os.path.splitext(img_name)[1].lower()
            # 只处理图片
            if ext not in [".jpg", ".jpeg", ".png", ".bmp", ".webp"]:
                continue
            paths.append(path)
        if paths:
            db[cls] = paths
            print(f"[OK] Loaded class {cls}, {len(paths)} images.")
    return db


# 多尺度模板匹配 + Top N 候选 + EfficientNet 二次验证
def match_template(target_path, template_path):
    """
    在目标大图中寻找模板。

    第一阶段：
        OpenCV 多尺度模板匹配

    第二阶段：
        Top N 候选区域使用 EfficientNet
        进行二次验证

    最终返回：
        综合评分最高的结果
    """

    # 参数
    TOP_N = 5
    # 读取目标图片
    target = cv2.imread(target_path, cv2.IMREAD_COLOR)
    # 读取模板
    template_original = cv2.imread(template_path, cv2.IMREAD_COLOR)
    if target is None:
        raise ValueError(f"无法读取目标图片: {target_path}")
    if template_original is None:
        raise ValueError(f"无法读取模板图片: {template_path}")
    # 获取目标尺寸
    target_height, target_width = (target.shape[:2])
    # 获取模板原始尺寸
    (template_height_original, template_width_original) = template_original.shape[:2]
    # OpenCV 多尺度匹配
    candidates = []
    scales = np.arange(SCALE_MIN, SCALE_MAX + SCALE_STEP / 2, SCALE_STEP)
    for scale in scales:
        # 计算缩放后的模板尺寸
        template_width = int(template_width_original * scale)
        template_height = int(template_height_original * scale)
        # 太小跳过
        if template_width < 10:
            continue
        if template_height < 10:
            continue
        # 大于目标图片跳过
        if template_width > target_width:
            continue
        if template_height > target_height:
            continue
        # 缩放模板
        template = cv2.resize(template_original, (template_width, template_height), interpolation=cv2.INTER_AREA)
        # OpenCV 模板匹配
        result = cv2.matchTemplate(target, template, cv2.TM_CCOEFF_NORMED)
        # 当前尺度获取 Top N
        scale_candidates = (
            get_top_candidates(
                result,
                top_n=TOP_N,
                min_distance=max(20, int(min(template_width, template_height) * 0.3))
            )
        )
        # 保存候选
        for candidate in scale_candidates:
            candidates.append({
                "template_score": candidate["score"],
                "x": candidate["x"],
                "y": candidate["y"],
                "width": template_width,
                "height": template_height,
                "scale": float(scale)
            })
    # 如果没有候选
    if not candidates:
        return {
            "score": -1.0,
            "x": 0,
            "y": 0,
            "width": 0,
            "height": 0,
            "scale": 1.0,
            "feature_score": 0.0
        }
    # OpenCV 第一阶段先排序
    candidates.sort(key=lambda x: x["template_score"], reverse=True)
    # 不需要把几十上百个候选全部送给 EfficientNet
    # 只保留整体排名靠前的一部分。
    candidates = candidates[:TOP_N]

    # 第二阶段 EfficientNet 特征提取
    template_feature = (extract_feature(template_path))
    # 最佳结果
    best_result = None
    best_final_score = -1.0
    # 对 Top N 候选进行二次验证
    for candidate in candidates:
        x = candidate["x"]
        y = candidate["y"]
        width = candidate["width"]
        height = candidate["height"]
        # 防止坐标越界
        x1 = max(0, x)
        y1 = max(0, y)
        x2 = min(target_width, x + width)
        y2 = min(target_height, y + height)
        if x2 <= x1:
            continue
        if y2 <= y1:
            continue
        # 裁剪候选区域
        crop = target[y1:y2, x1:x2]
        if crop.size == 0:
            continue
        # EfficientNet 提取候选区域特征
        try:
            crop_feature = (extract_feature_from_image(crop))
        except Exception as e:
            print(f"[WARNING] EfficientNet 特征提取失败: {e}")
            continue
        # EfficientNet 相似度
        feature_score = (calculate_feature_similarity(template_feature, crop_feature))

        # OpenCV 分数范围： -1 ~ 1
        # EfficientNet： -1 ~ 1
        # 两者直接加权。
        final_score = (candidate["template_score"] * TEMPLATE_WEIGHT + feature_score * FEATURE_WEIGHT)
        # 更新最佳结果
        if final_score > best_final_score:
            best_final_score = (float(final_score))
            best_result = {
                "score": float(final_score),
                "template_score": float(candidate["template_score"]),
                "feature_score": float(feature_score),
                "x": int(x1),
                "y": int(y1),
                "width": int(x2 - x1),
                "height": int(y2 - y1),
                "scale": float(candidate["scale"])
            }
    # 理论上不会发生
    if best_result is None:
        return {
            "score": -1.0,
            "x": 0,
            "y": 0,
            "width": 0,
            "height": 0,
            "scale": 1.0,
            "feature_score": 0.0
        }
    return best_result


# 在图片上画匹配框
def draw_match_result(image_path, result):
    """
    在原始目标图片上画出当前类别的匹配区域。

    一个 result 对应一张独立的结果图片。
    """

    # 每个类别都重新读取一次原始图片
    #
    # 这样 face、hand、eye 之间不会互相叠加。

    image = cv2.imread(image_path, cv2.IMREAD_COLOR)
    if image is None:
        print(f"[ERROR] 无法读取结果图片: {image_path}")
        return
    # 获取当前匹配结果
    x = result["x"]
    y = result["y"]
    width = result["width"]
    height = result["height"]
    cls = result["class"]
    score = result["score"]
    # 绘制当前类别的匹配框
    cv2.rectangle(image, (x, y), (x + width, y + height), (0, 0, 255), 3)
    # 显示类别和匹配分数
    text = (f"{cls} {score:.3f}")
    text_x = x
    text_y = max(y - 10, 30)
    cv2.putText(image, text, (text_x, text_y), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2, cv2.LINE_AA)
    # 输出文件名
    # 原文件名_face.jpg
    draw_name = os.path.splitext(os.path.basename(image_path))[0]
    output_name = (f"{draw_name}_{cls}.jpg")
    output_path = os.path.join(RESULT_DRAW_DIR, output_name)
    # 保存当前类别的独立图片
    success = cv2.imwrite(output_path, image)
    if success:
        print(f"结果图片: {output_path}")
    else:
        print(f"[ERROR] 结果图片保存失败: {output_path}")


# 原图复制并删除
# face_0.700_hand_0.500_eye_0.700_xxx_random.png
def rename_source_file(image_path, results):
    if not results:
        return
    filename = ""
    # 所有类别依次写入文件名
    for result in results:
        filename += (f"{result['class']}_{result['score']:.3f}_")
    # 原图名称
    original_name = os.path.splitext(os.path.basename(image_path))[0]
    # 去除原文件名中的特殊字符
    original_name = re.sub(r'[^\w\u4e00-\u9fff]', '', original_name)
    # 截取前10个字符
    original_name = (original_name[:10])
    # 生成6位随机字符串
    random_string = ''.join(random.choices(string.ascii_letters + string.digits, k=6))
    # 原图扩展名
    extension = os.path.splitext(os.path.basename(image_path))[1]
    # 最终文件名
    filename += (original_name + "_" + random_string + extension)
    # 目标路径
    output_path = os.path.join(RESULT_SOURCE_DIR, filename)
    # 先复制
    shutil.copy2(image_path, output_path)
    print(f"原图复制: {output_path}")
    # if os.path.exists(output_path):
    #     os.remove(image_path)
    #     print(f"原图已处理: {output_path}")


# 主识别函数
def recognize(image_path):
    if not os.path.exists(image_path):
        print(f"[ERROR] 目标图片不存在: {image_path}")
        return
    print()
    print("=" * 80)
    print(f"开始匹配: {image_path}")
    print("=" * 80)
    results = []

    # 遍历所有类别
    for cls, template_paths in (template_paths_db.items()):
        # 当前类别最佳结果
        class_best = None
        class_best_template = None
        # 遍历当前类别的所有模板
        for template_path in (template_paths):
            template_name = (os.path.basename(template_path))
            try:
                result = match_template(image_path, template_path)
                # 当前类别取最高匹配
                if (class_best is None or result["score"] > class_best["score"]):
                    class_best = result
                    class_best_template = (template_path)
            except Exception as e:
                print(f"[ERROR] {template_path}")
                print(f"原因: {e}")
        # 当前类别有结果
        if class_best is not None:
            results.append({
                "class": cls,
                "score": class_best["score"],
                "template_score": class_best.get("template_score", class_best["score"]),
                "feature_score": class_best.get("feature_score", 0.0),
                "x": class_best["x"],
                "y": class_best["y"],
                "width": class_best["width"],
                "height": class_best["height"],
                "scale": class_best["scale"],
                "template": class_best_template
            })
    # 按匹配度排序
    results.sort(key=lambda x: x["score"], reverse=True)
    # 输出结果
    print()
    print("-" * 80)
    if not results:
        print("没有找到任何模板。")
        print("-" * 80)
        return
    print("匹配结果：")
    for result in results:
        template_name = (os.path.basename(result["template"]))
        print(
            f"{result['class']}: "
            f"{result['score']:.3f}    "
            f"模板={result.get('template_score', 0):.3f}    "
            f"特征={result.get('feature_score', 0):.3f}    "
            f"位置=("
            f"{result['x']}, "
            f"{result['y']}"
            f")    "
            f"大小="
            f"{result['width']}x"
            f"{result['height']}    "
            f"缩放="
            f"{result['scale']:.2f}    "
            f"模板="
            f"{template_name}"
        )
    print("-" * 80)
    # 为每个类别生成结果图片
    print()
    print("正在生成匹配结果图片...")
    for result in results:
        draw_match_result(image_path, result)
    # 原图复制并重命名
    rename_source_file(image_path, results)
    print()
    print("匹配完成。")


feature_model = load_feature_model()
# 程序入口
if __name__ == "__main__":

    # 加载模板
    template_paths_db = (load_template_paths())
    # 检查模板
    if not template_paths_db:
        print("[ERROR] 没有加载到任何模板。")
        print(f"模板目录: {TEMPLATE_DIR}")
    else:
        # 依次识别 TARGET_DIR 下所有图片
        for img_name in os.listdir(TARGET_DIR):
            img_path = os.path.join(TARGET_DIR, img_name)
            # 跳过目录
            if not os.path.isfile(img_path):
                continue
            # 只处理图片
            ext = os.path.splitext(img_name)[1].lower()
            if ext not in [".jpg", ".jpeg", ".png", ".bmp", ".webp"]:
                continue
            print(f"\n正在识别: {img_name}")
            recognize(img_path)
            # break
