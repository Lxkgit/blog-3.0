#!/bin/bash

# 更新代码
/opt/docker/ci/shell/updateCode.sh -n blog

# 替换 Socket 代码为最新版本
rm -rf /opt/soft/socket/code
cp -r /opt/docker/ci/code/blog-3.0/socket /opt/soft/socket/code

# 处理 Shell 脚本
sed -i 's/\r$//' /opt/soft/socket/code/shell/*.sh
chmod +x /opt/soft/socket/code/shell/*.sh

# 停止当前 Socket 服务
# watchdog 会自动拉起新版本
/opt/soft/socket/restartSocket.sh