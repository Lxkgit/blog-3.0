import os
import numpy as np
from PIL import Image
from tensorflow.keras.applications import EfficientNetB0
from tensorflow.keras.applications.efficientnet import preprocess_input
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.models import Model
import cv2


# ================================
# 路径设置（100% 不会再找不到文件）
# ================================
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TEMPLATE_DIR = os.path.join(BASE_DIR, "template")   # 你的模板根目录
TARGET_DIR = os.path.join(BASE_DIR, "target")   # 你的模板根目录
IMG_SIZE = (224, 224)


# --------------------------------------------------------------------
# EfficientNet-B0 特征提取模型
# --------------------------------------------------------------------
def load_feature_model():
    base_model = EfficientNetB0(weights="imagenet", include_top=False, pooling='avg')
    model = Model(inputs=base_model.input, outputs=base_model.output)
    return model


feature_model = load_feature_model()


# --------------------------------------------------------------------
# 读取+预处理图片
# --------------------------------------------------------------------
def load_and_preprocess(path):
    img = Image.open(path).convert("RGB").resize(IMG_SIZE)
    img = img_to_array(img)
    img = np.expand_dims(img, axis=0)
    img = preprocess_input(img)
    return img


# --------------------------------------------------------------------
# 提取特征向量
# --------------------------------------------------------------------
def extract_feature(path):
    img = load_and_preprocess(path)
    feature = feature_model.predict(img, verbose=0)[0]
    # L2 归一化
    feature = feature / np.linalg.norm(feature)
    return feature


# --------------------------------------------------------------------
# 加载所有模板特征
# --------------------------------------------------------------------
def load_templates():
    db = {}
    for cls in os.listdir(TEMPLATE_DIR):
        cls_dir = os.path.join(TEMPLATE_DIR, cls)
        if not os.path.isdir(cls_dir):
            continue

        features = []
        for img_name in os.listdir(cls_dir):
            path = os.path.join(cls_dir, img_name)
            try:
                print(f"加载图片: {cls_dir} {img_name}")
                feat = extract_feature(path)
                features.append(feat)
            except:
                pass

        if len(features) > 0:
            db[cls] = features
            print(f"[OK] Loaded class {cls}, {len(features)} images.")
    return db


templates = load_templates()


# --------------------------------------------------------------------
# 余弦相似度
# --------------------------------------------------------------------
def cosine_similarity(a, b):
    return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))


# --------------------------------------------------------------------
# 主函数：识别
# --------------------------------------------------------------------
def recognize(image_path, threshold=0.70):
    query = extract_feature(image_path)

    best_class = None
    best_score = -1

    for cls, feats in templates.items():
        for feat in feats:
            score = cosine_similarity(query, feat)
            if score > best_score:
                best_score = score
                best_class = cls

    print(f"Best match: {best_class}, Score: {best_score:.3f}")

    if best_score >= threshold:
        return best_class, best_score
    else:
        return "Unknown", best_score


# --------------------------------------------------------------------
# 测试
# --------------------------------------------------------------------
if __name__ == "__main__":

    test_image = os.path.join(TARGET_DIR, "22.png")
    cls, score = recognize(test_image)
    print(f"Result: {cls}, similarity={score:.3f}")