import os
import shutil
import hashlib
import pickle
import random
import numpy as np
import face_recognition
import pymysql
from datetime import datetime

# ---------------- 配置 ----------------
IMAGE_DIR = "images"           # 待处理图片目录
CLUSTER_DIR = "clusters"       # 归类结果目录
FEATURE_DIR = "feature_data"   # 自定义特征模板目录 (目录名示例: 1-特征值, 2-特征值)
THRESHOLD_FACE = 0.5           # 人脸匹配欧氏距离阈值

# MySQL 连接配置
DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "123456",
    "database": "face_db",
    "charset": "utf8mb4"
}

# ---------------- 工具函数 ----------------
def calc_md5(img_path):
    """计算文件 MD5"""
    hash_md5 = hashlib.md5()
    with open(img_path, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()

def save_to_dir(target_dir, img_path, new_name):
    """将图片复制到目标目录并重命名"""
    os.makedirs(target_dir, exist_ok=True)
    dest_path = os.path.join(target_dir, new_name)
    shutil.copy(img_path, dest_path)

def face_match(encoding, known_encodings):
    """匹配人脸向量，返回 hash_key 或 None"""
    for hk, vec in known_encodings.items():
        if np.linalg.norm(encoding - vec) < THRESHOLD_FACE:
            return hk
    return None

def feature_match(img_name, feature_templates):
    """
    匹配自定义特征值
    img_name: 图片文件名
    feature_templates: 特征模板列表，顺序对应 i-特征值 目录
    返回列表，例如 [0,1] 表示第1个特征未匹配，第2个匹配
    """
    result = []
    for template_name in feature_templates:
        if template_name in img_name:
            result.append(1)
        else:
            result.append(0)
    return result

# ---------------- 主处理函数 ----------------
def process_images():
    os.makedirs(CLUSTER_DIR, exist_ok=True)

    # 连接 MySQL
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()

    # 1️⃣ 加载自定义特征模板目录
    feature_dirs = sorted([d for d in os.listdir(FEATURE_DIR) if os.path.isdir(os.path.join(FEATURE_DIR, d))])
    print(f"✅ 已加载 {len(feature_dirs)} 个自定义特征目录：{feature_dirs}")

    # 2️⃣ 加载历史人脸向量
    cursor.execute("SELECT hash_key, encoding FROM python_face_data")
    known_encodings = {}
    for hk, enc_bytes in cursor.fetchall():
        known_encodings[hk] = pickle.loads(enc_bytes)
    print(f"✅ 已加载 {len(known_encodings)} 条历史人脸向量")

    # 3️⃣ 遍历图片
    for filename in os.listdir(IMAGE_DIR):
        if not filename.lower().endswith((".jpg", ".jpeg", ".png")):
            continue
        img_path = os.path.join(IMAGE_DIR, filename)

        # 3.1 MD5 去重
        file_md5 = calc_md5(img_path)
        cursor.execute("SELECT id, file_count FROM python_img_md5 WHERE file_md5=%s", (file_md5,))
        row = cursor.fetchone()
        if row:
            md5_id, count = row
            cursor.execute("UPDATE python_img_md5 SET file_count=%s WHERE id=%s", (count+1, md5_id))
            conn.commit()
            print(f"⚠️ {filename} 已处理过，file_count +1")
            continue
        else:
            cursor.execute("INSERT INTO python_img_md5 (file_md5, file_count) VALUES (%s,%s)", (file_md5,1))
            conn.commit()

        # 3.2 加载图片
        image = face_recognition.load_image_file(img_path)
        faces = face_recognition.face_encodings(image)

        # 3.3 匹配人脸
        face_hk = None
        if faces:
            for encoding in faces:
                hk = face_match(encoding, known_encodings)
                if hk:
                    face_hk = hk
                    break

        # 3.4 匹配自定义特征
        feature_result = feature_match(filename, feature_dirs)

        # 3.5 判断归类
        if face_hk:
            # 匹配到人脸 → face/<hash_key> 目录
            target_dir = os.path.join(CLUSTER_DIR, "face", face_hk)
        elif any(feature_result):
            # 无人脸但匹配到特征 → no_face 目录
            target_dir = os.path.join(CLUSTER_DIR, "no_face")
        else:
            # 全部没匹配 → unknown 目录
            target_dir = os.path.join(CLUSTER_DIR, "unknown")

        # 3.6 文件重命名（加6位随机码）
        timestamp = datetime.now().strftime("%Y-%m-%d_%H:%M:%S")
        feature_str = "_".join(str(f) for f in feature_result) if feature_result else "_".join(["0"]*len(feature_dirs))
        ext = os.path.splitext(filename)[1]
        random_code = f"{random.randint(0, 999999):06d}"  # 6位随机码
        new_name = f"{timestamp}_{feature_str}_{random_code}{ext}"

        # 3.7 保存图片
        save_to_dir(target_dir, img_path, new_name)

        # 3.8 新人脸保存向量
        if not face_hk and faces:
            encoding = faces[0]
            face_hk = hashlib.sha256(encoding.tobytes()).hexdigest()
            known_encodings[face_hk] = encoding
            enc_bytes = pickle.dumps(encoding)
            cursor.execute("INSERT INTO python_face_data (hash_key, encoding, first_img) VALUES (%s,%s,%s)",
                           (face_hk, enc_bytes, filename))
            conn.commit()

        # 3.9 保存归类记录
        matched_type = "face" if face_hk else ("feature" if any(feature_result) else "unknown")
        cursor.execute(
            "INSERT INTO python_img_classification (file_name, hash_key, matched_type) VALUES (%s,%s,%s)",
            (new_name, face_hk if face_hk else None, matched_type)
        )
        conn.commit()

        print(f"✅ {filename} → {target_dir}/{new_name} | 匹配类型: {matched_type} | 特征: {feature_result}")

    # 关闭数据库连接
    cursor.close()
    conn.close()
    print("🎉 全部处理完成！")

# ---------------- main ----------------
if __name__ == "__main__":
    process_images()
