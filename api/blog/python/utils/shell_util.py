import subprocess
from logger.log_util import logger


# 执行Shell脚本
def execute_shell_script(shell_script_path):
    """同步执行Shell脚本并返回结果"""
    try:
        # 执行脚本并捕获输出
        result = subprocess.run(
            shell_script_path,
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,  # 合并标准输出和错误输出
            text=True,
            check=False
        )
        logger.info(f"脚本执行完成，执行输出: {result.stdout}")
        logger.info(f"脚本执行完成，返回码: {result.returncode}")
        return {
            "success": result.returncode == 0,
            "output": result.stdout,
            "code": result.returncode
        }

    except Exception as e:
        logger.error(f"执行脚本错误: {str(e)}")
        return {
            "success": False,
            "output": str(e),
            "code": -1
        }
