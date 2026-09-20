#!/bin/bash
set -e

# ==================== 配置 ====================
WORK_DIR="/opt/soft/wireguard"
CLIENT_DIR="$WORK_DIR/client/raspberrypi"
CONF_FILE="$CLIENT_DIR/raspberrypi.conf"
PRIVATE_KEY_FILE="$CLIENT_DIR/client_private.key"
WG_CONF="/etc/wireguard/wg0.conf"

# ==================== 检查 root ====================
checkRoot() {
  if [[ $EUID -ne 0 ]]; then
    echo "请使用 root 权限运行"
    exit 1
  fi
}

# ==================== 安装软件 ====================
installSoftware() {
  echo "=== 安装 WireGuard ==="
  apt-get update -y
  apt-get install -y wireguard iptables
}

# ==================== 开启 IP 转发 ====================
enableForward() {
  echo "=== 开启 IP 转发 ==="
  echo "net.ipv4.ip_forward = 1" > /etc/sysctl.d/99-ipforward.conf
  sysctl -p /etc/sysctl.d/99-ipforward.conf
}

# ==================== 检测网卡 ====================
detectInterface() {
  echo "=== 检测网卡 ==="
  IFACE=$(ip route get 8.8.8.8 2>/dev/null | awk '{print $5; exit}')

  if [[ -z "$IFACE" ]]; then
    if ip link show wlan0 &>/dev/null && [[ $(cat /sys/class/net/wlan0/operstate 2>/dev/null) == "up" ]]; then
      IFACE="wlan0"
    elif ip link show eth0 &>/dev/null && [[ $(cat /sys/class/net/eth0/operstate 2>/dev/null) == "up" ]]; then
      IFACE="eth0"
    else
      echo "错误：无法自动检测网卡，请手动修改脚本中的 IFACE"
      exit 1
    fi
  fi

  echo "检测到网卡: $IFACE"
}

# ==================== 检查配置文件 ====================
checkConfigFile() {
  if [[ ! -f "$CONF_FILE" ]]; then
    echo "错误：未找到配置文件 $CONF_FILE"
    echo "请先把服务器生成的 client/raspberrypi 目录上传到 $WORK_DIR/client/ 下"
    exit 1
  fi

  if [[ ! -f "$PRIVATE_KEY_FILE" ]]; then
    echo "错误：未找到私钥文件 $PRIVATE_KEY_FILE"
    exit 1
  fi

  echo "找到配置文件: $CONF_FILE"
  echo "找到私钥文件: $PRIVATE_KEY_FILE"
}

# ==================== 生成最终配置 ====================
createConfig() {
  echo "=== 生成 WireGuard 配置 ==="

  PRIVATE_KEY=$(cat "$PRIVATE_KEY_FILE")
  ADDRESS=$(grep -E "^Address" "$CONF_FILE" | awk '{print $3}')
  PUBLIC_KEY=$(grep -E "^PublicKey" "$CONF_FILE" | awk '{print $3}')
  ENDPOINT=$(grep -E "^Endpoint" "$CONF_FILE" | awk '{print $3}')
  ALLOWED_IPS=$(grep -E "^AllowedIPs" "$CONF_FILE" | awk '{print $3}')
  KEEPALIVE=$(grep -E "^PersistentKeepalive" "$CONF_FILE" | awk '{print $3}')

  cat > "$WG_CONF" << EOF
[Interface]
PrivateKey = ${PRIVATE_KEY}
Address = ${ADDRESS}
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -A FORWARD -o wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ${IFACE} -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -D FORWARD -o wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ${IFACE} -j MASQUERADE

[Peer]
PublicKey = ${PUBLIC_KEY}
Endpoint = ${ENDPOINT}
AllowedIPs = ${ALLOWED_IPS}
PersistentKeepalive = ${KEEPALIVE}
EOF

  chmod 600 "$WG_CONF"
  echo "配置文件已生成: $WG_CONF"
}

# ==================== 启动服务 ====================
startService() {
  echo "=== 启动 WireGuard ==="
  systemctl enable wg-quick@wg0
  systemctl restart wg-quick@wg0
}

# ==================== 检查状态 ====================
checkStatus() {
  echo ""
  echo "============================================"
  echo "安装完成，当前状态："
  wg show
  echo "============================================"
  echo "测试连通性（ping 服务器）："
  ping -c 3 10.66.66.1 || echo "暂时 ping 不通，请检查服务器是否正常"
}

# ==================== 主函数 ====================
main() {
  checkRoot
  installSoftware
  enableForward
  detectInterface
  checkConfigFile
  createConfig
  startService
  checkStatus
}

main