#!/bin/bash
set -e

WG_PORT=51820
CLIENT_NAME=""
CLIENT_IP=""

# ==================== 参数解析 ====================
parseArgs() {
  while [[ $# -gt 0 ]]; do
      case $1 in
          -n|--name)
              CLIENT_NAME="$2"
              shift 2
              ;;
          -ip|--ip)
              CLIENT_IP="$2"
              shift 2
              ;;
          -h|--help)
              echo "使用方法:"
              echo "  $0                     # 名称和IP全部自动生成"
              echo "  $0 -n 名称             # 指定名称，IP自动"
              echo "  $0 -ip IP地址          # 指定IP，名称自动"
              echo "  $0 -n 名称 -ip IP地址  # 全部手动指定"
              exit 0
              ;;
          *)
              echo "未知参数: $1"
              exit 1
              ;;
      esac
  done
}

# 初始化
initEnv() {
  if [[ $EUID -ne 0 ]]; then
      echo "请使用 root 权限运行"
      exit 1
  fi

  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  KEY_DIR="$SCRIPT_DIR/keys"
  CLIENT_BASE_DIR="$SCRIPT_DIR/client"

  if [[ ! -f "$KEY_DIR/server_public.key" ]]; then
      echo "错误：未找到服务器公钥，请先运行服务端安装脚本"
      exit 1
  fi
}

# 计算下一个 IP
calcNextIP() {
  MAX_IP=$(grep -oP '10\.66\.66\.\d+' /etc/wireguard/wg0.conf "$CLIENT_BASE_DIR"/*/*.conf 2>/dev/null | \
           grep -oP '\d+$' | sort -n | tail -1)

  if [[ -z "$MAX_IP" ]]; then
      NEXT_IP=10
  else
      NEXT_IP=$((MAX_IP + 1))
  fi

  if [[ -z "$CLIENT_IP" ]]; then
      CLIENT_IP="10.66.66.${NEXT_IP}"
  fi
}

# 计算下一个名称
calcNextName() {
  if [[ -z "$CLIENT_NAME" ]]; then
      MAX_NUM=$(ls -d "$CLIENT_BASE_DIR"/client-*/ 2>/dev/null | grep -oP 'client-\K\d+' | sort -n | tail -1)
      if [[ -z "$MAX_NUM" ]]; then
          NEXT_NUM=1
      else
          NEXT_NUM=$((MAX_NUM + 1))
      fi
      CLIENT_NAME="client-${NEXT_NUM}"
  fi

  CLIENT_DIR="$CLIENT_BASE_DIR/${CLIENT_NAME}"
  mkdir -p "$CLIENT_DIR"
}

# 检查 IP 是否冲突
checkIPConflict() {
  if grep -q "${CLIENT_IP}/32" /etc/wireguard/wg0.conf 2>/dev/null; then
      echo "错误：IP ${CLIENT_IP} 已被使用，请换一个"
      exit 1
  fi
}

# 生成客户端密钥和配置
createClient() {
  echo "正在添加客户端..."
  echo "  名称: $CLIENT_NAME"
  echo "  IP  : $CLIENT_IP"

  umask 077

  # 生成密钥
  wg genkey | tee "$CLIENT_DIR/${CLIENT_NAME}_private.key" | wg pubkey > "$CLIENT_DIR/${CLIENT_NAME}_public.key"

  SERVER_PUB=$(cat "$KEY_DIR/server_public.key")
  CLIENT_PRIV=$(cat "$CLIENT_DIR/${CLIENT_NAME}_private.key")
  CLIENT_PUB=$(cat "$CLIENT_DIR/${CLIENT_NAME}_public.key")

  SERVER_PUBLIC_IP=$(curl -s --max-time 5 https://api.ipify.org || curl -s --max-time 5 ifconfig.me)

  # 生成配置文件
  cat > "$CLIENT_DIR/${CLIENT_NAME}.conf" << EOF
[Interface]
PrivateKey = ${CLIENT_PRIV}
Address = ${CLIENT_IP}/24

[Peer]
PublicKey = ${SERVER_PUB}
Endpoint = ${SERVER_PUBLIC_IP}:${WG_PORT}
AllowedIPs = 10.66.66.0/24, 192.168.0.0/24
PersistentKeepalive = 25
EOF

  chmod 600 "$CLIENT_DIR/${CLIENT_NAME}.conf"
  chmod 600 "$CLIENT_DIR/${CLIENT_NAME}_private.key"
}

# 添加到服务器
addPeerToServer() {
  CLIENT_PUB=$(cat "$CLIENT_DIR/${CLIENT_NAME}_public.key")

  cat >> /etc/wireguard/wg0.conf << EOF

[Peer]
# ${CLIENT_NAME}
PublicKey = ${CLIENT_PUB}
AllowedIPs = ${CLIENT_IP}/32
EOF

  systemctl restart wg-quick@wg0
}

# 主函数
main() {
  parseArgs "$@"
  initEnv
  calcNextIP
  calcNextName
  checkIPConflict
  createClient
  addPeerToServer

  echo "============================================"
  echo "客户端添加成功！"
  echo "设备名称 : $CLIENT_NAME"
  echo "虚拟 IP  : $CLIENT_IP"
  echo "配置文件 : $CLIENT_DIR/${CLIENT_NAME}.conf"
  echo "私钥文件 : $CLIENT_DIR/${CLIENT_NAME}_private.key"
  echo "公钥文件 : $CLIENT_DIR/${CLIENT_NAME}_public.key"
  echo "============================================"
  cat "$CLIENT_DIR/${CLIENT_NAME}.conf"
}

main "$@"