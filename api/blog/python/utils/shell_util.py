import subprocess
from logger.log_util import logger


# 执行Shell脚本
def execute_shell_script(shell_script_path):
    logger.info(f"开始执行 shell: {shell_script_path}")

    process = subprocess.Popen(
        shell_script_path,
        shell=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1,          # 行缓冲
        universal_newlines=True
    )

    for line in process.stdout:
        logger.info(line.rstrip())

    process.wait()
    code = process.returncode

    logger.info(f"脚本执行完成，返回码: {code}")

    return {
        "success": code == 0,
        "code": code
    }

