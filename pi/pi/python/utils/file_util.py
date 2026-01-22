import os
import shutil
from logger.log_util import logger
from pathlib import Path


# 获取指定目录下前x文件的名称
def get_path_first_x_filename(directory, count):
    # 获取目录下所有条目
    all_entries = os.listdir(directory)

    # 过滤出文件并排序
    files = sorted(
        [entry for entry in all_entries if os.path.isfile(os.path.join(directory, entry))],
        key=lambda f: f.lower()  # 不区分大小写排序
    )

    # 返回前x个文件
    return files[:count]


# 文件或目录移动方法
def move_file_or_directory(source_path, destination_path):
    """
    移动文件或目录到指定目录（总是将destination_path视为目录）
    参数:
        source_path (str): 源文件/目录路径
        destination_path (str): 目标目录路径（总是作为目录处理）
    返回:
        tuple: (success: bool, message: str)
    """
    # 检查源路径是否存在
    if not os.path.exists(source_path):
        logger.info(f"错误：源路径 '{source_path}' 不存在")
        return False

    try:
        # 确保目标路径是目录格式（去除可能的尾部分隔符）
        destination_path = destination_path.rstrip(os.sep)

        # 创建目标目录（包括所有父目录）
        os.makedirs(destination_path, exist_ok=True)

        # 构建完整目标路径（目标目录 + 源文件名）
        final_path = os.path.join(destination_path, os.path.basename(source_path))

        # 执行移动操作
        shutil.move(source_path, final_path)

        logger.info(f"成功移动 '{source_path}' 到目录 '{destination_path}'")

        return True

    except PermissionError:
        return False, f"权限错误：无法移动 '{source_path}'，请检查文件权限"

    except FileNotFoundError as exception:
        return False, f"路径错误：{str(exception)}"

    except Exception as exception:
        return False, f"移动失败：{str(exception)}"


# 文件或目录复制方法
def copy_file_or_directory(source_path, destination_path):
    """
    复制文件或目录到指定目录（总是将 destination_path 视为目录）

    参数:
        source_path (str): 源文件/目录路径
        destination_path (str): 目标目录路径（总是作为目录处理）

    返回:
        tuple: (success: bool, message: str)
    """
    # 检查源路径是否存在
    if not os.path.exists(source_path):
        logger.info(f"错误：源路径 '{source_path}' 不存在")
        return False

    try:
        # 确保目标路径是目录格式（去除可能的尾部分隔符）
        destination_path = destination_path.rstrip(os.sep)

        # 创建目标目录（包括所有父目录）
        os.makedirs(destination_path, exist_ok=True)

        # 构建完整目标路径（目标目录 + 源文件名）
        final_path = os.path.join(destination_path, os.path.basename(source_path))

        shutil.copy2(source_path, final_path)

        logger.info(f"成功复制 '{source_path}' 到目录 '{destination_path}'")
        return True

    except PermissionError:
        return False, f"权限错误：无法复制 '{source_path}'，请检查文件权限"

    except FileNotFoundError as exception:
        return False, f"路径错误：{str(exception)}"

    except Exception as exception:
        return False, f"复制失败：{str(exception)}"

# 删除目录或文件
def delete_file_or_directory(path):
    """
    安全删除文件或目录（包含所有内容）
    参数:
        path (str): 要删除的文件或目录路径
    返回:
        bool: 删除成功返回True，否则返回False
    """
    try:
        logger.info(f"尝试删除文件或目录: {path}")
        # 转换为绝对路径
        target = Path(path).resolve()

        # 基本安全校验
        if not target.exists():
            logger.warning(f"路径不存在: {target}")
            return False

        # 关键目录保护（防止误删系统文件）
        protected_paths = [
            Path("/"),
            Path.home(),
            Path("/etc"),
            Path("/bin"),
            Path("/usr"),
            Path("/var"),
            Path("/lib"),
        ]

        # 检查是否尝试删除受保护路径
        for protected in protected_paths:
            protected_abs = protected.resolve()
            try:
                if target == protected_abs:
                    logger.error(f"尝试删除受保护路径: {target}")
                    return False
            except ValueError:
                # 处理路径解析错误
                continue

        # 执行删除操作
        if target.is_file():
            os.chmod(target, 0o777)  # 确保有权限删除
            os.remove(target)
            logger.info(f"文件已删除: {target}")
        elif target.is_dir():
            shutil.rmtree(target, ignore_errors=False)
            logger.info(f"目录已删除: {target}")
        else:
            logger.warning(f"特殊文件类型，跳过: {target}")
            return False

        return True

    except PermissionError as e:
        logger.error(f"权限不足，无法删除: {str(e)}")
        return False
    except Exception as e:
        logger.error(f"文件删除错误: {str(e)}", exc_info=True)
        return False
