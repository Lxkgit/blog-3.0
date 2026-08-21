import os
import re
import cv2
import shutil
import random
import string
import numpy as np
from PIL import Image
from fileinput import filename
from tensorflow.keras.models import Model
from tensorflow.keras.applications import EfficientNetB0
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.applications.efficientnet import preprocess_input


# 路径设置
# BASE_DIR = r"D:\project\image"
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

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


# EfficientNet 提取特征
def extract_feature(path):
    img = load_and_preprocess(path)
    feature_model = load_feature_model()
    feature = feature_model.predict(img, verbose=0)[0]
    # L2 归一化
    norm = np.linalg.norm(feature)
    if norm != 0:
        feature = feature / norm
    return feature


# 加载模板图片路径
def load_template_paths():
    db = {}
    if not os.path.exists(TEMPLATE_DIR):
        print(f"[ERROR] 模板目录不存在: {TEMPLATE_DIR}")
        return db
    for cls in os.listdir(TEMPLATE_DIR):
        cls_dir = os.path.join(TEMPLATE_DIR, cls)
        if not os.path.isdir(cls_dir):
            continue
        paths = []
        for img_name in os.listdir(cls_dir):
            path = os.path.join(cls_dir, img_name)
            if not os.path.isfile(path):
                continue
            ext = os.path.splitext(img_name)[1].lower()
            if ext not in [".jpg", ".jpeg", ".png", ".bmp", ".webp"]:
                continue
            paths.append(path)
        if paths:
            db[cls] = paths
            print(f"[OK] Loaded class {cls}, {len(paths)} images.")
    return db


# 多尺度模板匹配
def match_template(target_path, template_path):
    """
    在目标大图中寻找完整模板。
    target：保持原始尺寸，不进行缩放
    template：尝试不同缩放比例
    返回： 最高匹配结果
    """

    # 读取目标图片
    target = cv2.imread(target_path, cv2.IMREAD_COLOR)
    # 读取模板
    template_original = cv2.imread(template_path, cv2.IMREAD_COLOR)
    if target is None:
        raise ValueError(f"无法读取目标图片: {target_path}")
    if template_original is None:
        raise ValueError(f"无法读取模板图片: {template_path}")
    # 获取目标图片尺寸
    target_height, target_width = (target.shape[:2])
    # 获取模板原始尺寸
    template_height_original, template_width_original = (template_original.shape[:2])
    # 初始化最佳结果
    best_score = -1.0
    best_x = 0
    best_y = 0
    best_width = 0
    best_height = 0
    best_scale = 1.0
    # 生成缩放比例
    scales = np.arange(SCALE_MIN, SCALE_MAX + SCALE_STEP / 2, SCALE_STEP)
    # 多尺度搜索
    for scale in scales:
        # 模板缩放后的尺寸
        template_width = int(template_width_original * scale)
        template_height = int(template_height_original * scale)
        # 模板太小，跳过
        if template_width < 10:
            continue
        if template_height < 10:
            continue
        # 模板比目标图还大，跳过
        if template_width > target_width:
            continue
        if template_height > target_height:
            continue
        # 缩放模板
        template = cv2.resize(template_original, (template_width, template_height), interpolation=cv2.INTER_AREA)
        # 模板匹配
        result = cv2.matchTemplate(target, template, cv2.TM_CCOEFF_NORMED)
        # 找到当前尺度最高匹配
        _, max_val, _, max_loc = (cv2.minMaxLoc(result))
        # 更新全局最高匹配
        if max_val > best_score:
            best_score = float(max_val)
            best_x = int(max_loc[0])
            best_y = int(max_loc[1])
            best_width = int(template_width)
            best_height = int(template_height)
            best_scale = float(scale)
    # 返回结果
    return {
        "score": best_score,
        "x": best_x,
        "y": best_y,
        "width": best_width,
        "height": best_height,
        "scale": best_scale
    }


# 在图片上画匹配框
def draw_match_result(image_path, result):
    """
    在原始目标图片上画出匹配区域。
    """
    image = cv2.imread(image_path, cv2.IMREAD_COLOR)
    if image is None:
        print(f"[ERROR] 无法读取结果图片: {image_path}")
        return
    x = result["x"]
    y = result["y"]
    width = result["width"]
    height = result["height"]
    cls = result["class"]
    score = result["score"]
    # 绘制矩形
    cv2.rectangle(image, (x, y), (x + width, y + height), (0, 0, 255), 3)
    # 显示文字
    text = (f"{cls} {score:.3f}")
    text_x = x
    text_y = max(y - 10, 30)
    cv2.putText(image, text, (text_x, text_y), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2, cv2.LINE_AA)
    # 保存结果
    draw_name = os.path.splitext(os.path.basename(image_path))[0]
    extension = os.path.splitext(os.path.basename(image_path))[1]
    output_name = (f"{draw_name}_{cls}{extension}")
    output_path = os.path.join(RESULT_DRAW_DIR, output_name)
    cv2.imwrite(output_path, image)
    print(f"识别图片: {output_path}")


# 原图复制并删除
def rename_source_file(image_path, results):
    if not results:
        return
    filename = ""
    results.sort(key=lambda x: x["class"].lower())
    for result in results:
        filename += f"{result['class']}_{result['score']:.3f}_"
    # 原图名称
    original_name = os.path.splitext(os.path.basename(image_path))[0]
    # 去除原文件名中的特殊字符 只保留：中文 英文 数字 下划线
    original_name = re.sub(r'[^\w\u4e00-\u9fff]', '', original_name)
    # 截取前10个字符
    original_name = original_name[:10]
    # 生成6位随机字符串
    random_string = ''.join(random.choices(string.ascii_letters + string.digits, k=6))
    # 原图扩展名
    extension = os.path.splitext(os.path.basename(image_path))[1]
    filename += original_name + "_" + random_string + extension
    # 目标路径
    output_path = os.path.join(RESULT_SOURCE_DIR, filename)
    # 先复制
    shutil.copy2(image_path, output_path)
    print(f"移动原图: {output_path}")
    # 确认复制成功后再删除原图
    # if os.path.exists(output_path):
    #     os.remove(image_path)
    #     print(f"原图已处理: {output_path}")


# 主识别函数
def recognize(image_path):
    if not os.path.exists(image_path):
        print(f"[ERROR] 目标图片不存在: {image_path}")
        return
    print(f"开始匹配: {image_path}")
    results = []
    # 遍历所有类别
    for cls, template_paths in template_paths_db.items():
        class_best = None
        class_best_template = None
        # 遍历当前类别的所有模板
        for template_path in template_paths:
            template_name = os.path.basename(template_path)
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
                "x": class_best["x"],
                "y": class_best["y"],
                "width": class_best["width"],
                "height": class_best["height"],
                "scale": class_best["scale"],
                "template": (class_best_template)
            })
    # 按匹配度排序
    results.sort(key=lambda x: x["score"], reverse=True)
    # 输出结果
    if not results:
        print("没有找到任何模板。")
        return
    print("匹配结果：")
    for result in results:
        template_name = os.path.basename(result["template"])
        print(
            f"{result['class']}: {result['score']:.3f}    "
            f"位置=({result['x']}, {result['y']})    "
            f"大小={result['width']}x{result['height']}    "
            f"缩放={result['scale']:.2f}    "
            f"模板={template_name}"
        )
    # 为每个类别生成结果图片
    print("正在生成匹配结果图片...")
    for result in results:
        draw_match_result(image_path, result)
    rename_source_file(image_path, results)


# 程序入口
if __name__ == "__main__":

    # 加载模板
    template_paths_db = (load_template_paths())
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
        recognize(img_path)
        print("-" * 110)
        break
