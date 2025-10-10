CREATE TABLE python_img_md5
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_md5    CHAR(32) NOT NULL UNIQUE COMMENT '文件MD5',
    file_count  INT      DEFAULT 1 COMMENT '重复文件数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片MD5去重表';

CREATE TABLE python_face_data
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    hash_key    CHAR(64) NOT NULL UNIQUE COMMENT '人脸唯一哈希',
    encoding    LONGBLOB NOT NULL COMMENT '人脸128维向量序列化存储',
    first_img   VARCHAR(255) COMMENT '首次出现图片文件名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人脸向量存储表';


CREATE TABLE python_img_classification
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name    VARCHAR(255) NOT NULL COMMENT '图片文件名',
    hash_key     CHAR(64) COMMENT '匹配的人脸哈希或特征模板名',
    matched_type ENUM('face','feature','unknown') DEFAULT 'unknown' COMMENT '匹配类型',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片分类记录表';

CREATE TABLE python_feature_template
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL UNIQUE COMMENT '特征模板名称',
    sample_count  INT      DEFAULT 0 COMMENT '样本图片数量',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义特征模板表';
