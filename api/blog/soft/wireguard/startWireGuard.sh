#!/bin/bash
set -e

WG_PORT=51820
SERVER_IP="10.66.66.1"

installWireGuard() {
  echo "=== 开始安装 WireGuard 服务端 ==="
  if [[ $EUID -ne 0 ]]; then
     echo "请使用 root 权限运行"
     exit 1
  fi

  apt-get update -y
  apt-get install -y wireguard qrencode iptables curl

  echo "net.ipv4.ip_forward = 1" > /etc/sysctl.d/99-wireguard.conf
  sysctl -p /etc/sysctl.d/99-wireguard.conf

  mkdir -p /etc/wireguard
  chmod 700 /etc/wireguard
}

createServerKey() {
  echo "=== 校验生成服务器密钥 ==="
  umask 077

  # 获取脚本所在目录
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  KEY_DIR="$SCRIPT_DIR/keys"

  # 判断脚本目录下是否已有密钥
  if [[ ! -f "$KEY_DIR/server_private.key" || ! -f "$KEY_DIR/server_public.key" ]]; then
      echo "未找到服务器密钥，正在生成..."
      mkdir -p "$KEY_DIR"
      wg genkey | tee "$KEY_DIR/server_private.key" | wg pubkey > "$KEY_DIR/server_public.key"
  else
      echo "检测到已有服务器密钥，跳过生成"
  fi

  # 同步到 /etc/wireguard 供服务使用
  cp "$KEY_DIR/server_private.key" /etc/wireguard/
  cp "$KEY_DIR/server_public.key" /etc/wireguard/
  chmod 600 /etc/wireguard/server_private.key
  chmod 644 /etc/wireguard/server_public.key
}

createServerConfig() {
  echo "=== 配置 WireGuard 服务端 ==="

  SERVER_PRIV=$(cat /etc/wireguard/server_private.key)
  SERVER_PUB=$(cat /etc/wireguard/server_public.key)

  PUBLIC_IF=$(ip route get 8.8.8.8 | awk '{print $5; exit}')
  echo "检测到公网网卡: $PUBLIC_IF"

  cat > /etc/wireguard/wg0.conf << EOF
[Interface]
Address = ${SERVER_IP}/24
ListenPort = ${WG_PORT}
PrivateKey = ${SERVER_PRIV}
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ${PUBLIC_IF} -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ${PUBLIC_IF} -j MASQUERADE
EOF

  chmod 600 /etc/wireguard/wg0.conf
  systemctl enable wg-quick@wg0
  systemctl restart wg-quick@wg0

  echo "服务器公钥: $SERVER_PUB"
}

main() {
  installWireGuard
  createServerKey
  createServerConfig

  echo ""
  echo "============================================"
  echo "WireGuard 服务端安装完成！"
  echo "密钥目录: $(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/keys"
  wg show
  echo "============================================"
}

main