#!/bin/bash
set -e

WG_PORT=51820
CLIENT_IP="10.66.66.2"
CLIENT_NAME="raspberrypi"

checkServer() {
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  KEY_DIR="$SCRIPT_DIR/keys"

  if [[ ! -f "$KEY_DIR/server_public.key" ]]; then
    echo "错误：未找到服务器公钥，请先运行服务端安装脚本"
    exit 1
  fi
}

initDir() {
  echo "=== 初始化目录 ==="
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  CLIENT_DIR="$SCRIPT_DIR/client/${CLIENT_NAME}"
  KEY_DIR="$SCRIPT_DIR/keys"
  mkdir -p "$CLIENT_DIR"
  echo "客户端目录: $CLIENT_DIR"
}

createClientKey() {
  echo "=== 校验生成树莓派密钥 ==="
  umask 077

  if [[ ! -f "$CLIENT_DIR/client_private.key" || ! -f "$CLIENT_DIR/client_public.key" ]]; then
      echo "未找到树莓派密钥，正在生成..."
      wg genkey | tee "$CLIENT_DIR/client_private.key" | wg pubkey > "$CLIENT_DIR/client_public.key"
  else
      echo "检测到已有树莓派密钥，跳过生成"
  fi
}

createClientConfig() {
  echo "=== 生成树莓派配置文件 ==="

  SERVER_PUB=$(cat "$KEY_DIR/server_public.key")
  CLIENT_PRIV=$(cat "$CLIENT_DIR/client_private.key")
  CLIENT_PUB=$(cat "$CLIENT_DIR/client_public.key")

  SERVER_PUBLIC_IP=$(curl -s --max-time 5 https://api.ipify.org || curl -s --max-time 5 ifconfig.me)
  echo "服务器公网 IP: $SERVER_PUBLIC_IP"

  cat > "$CLIENT_DIR/${CLIENT_NAME}.conf" << EOF
[Interface]
PrivateKey = ${CLIENT_PRIV}
Address = ${CLIENT_IP}/24

[Peer]
PublicKey = ${SERVER_PUB}
Endpoint = ${SERVER_PUBLIC_IP}:${WG_PORT}
AllowedIPs = 10.66.66.0/24
PersistentKeepalive = 25
EOF

  chmod 600 "$CLIENT_DIR/${CLIENT_NAME}.conf"
  echo "配置文件已生成: $CLIENT_DIR/${CLIENT_NAME}.conf"
}

addPeerToServer() {
  echo "=== 将树莓派添加到服务器 ==="

  CLIENT_PUB=$(cat "$CLIENT_DIR/client_public.key")

  if ! grep -q "$CLIENT_PUB" /etc/wireguard/wg0.conf 2>/dev/null; then
    cat >> /etc/wireguard/wg0.conf << EOF

[Peer]
# 树莓派
PublicKey = ${CLIENT_PUB}
AllowedIPs = ${CLIENT_IP}/32, 192.168.0.0/24
EOF
    systemctl restart wg-quick@wg0
    echo "已将树莓派公钥添加到服务器配置"
  else
    echo "服务器配置中已存在该树莓派公钥，跳过添加"
  fi
}

main() {
  checkServer
  initDir
  createClientKey
  createClientConfig
  addPeerToServer

  echo ""
  echo "============================================"
  echo "树莓派配置生成完成！"
  echo "配置文件: $CLIENT_DIR/${CLIENT_NAME}.conf"
  echo "私钥文件: $CLIENT_DIR/client_private.key"
  echo "公钥文件: $CLIENT_DIR/client_public.key"
  echo "============================================"
}

main
