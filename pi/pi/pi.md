# pi.sh 部署命令

## 树莓派安装系统需要设置root账号密码
```shell
sudo passwd root
```

## 下载rz、sz命令
```shell
apt-get install lrzsz
```

## 执行脚本
```shell
chmod +x pi.sh
sed -i 's/\r$//' pi.sh
nohup ./pi.sh >my.log 2>&1 &
tail -f my.log
```
# 树莓派挂载NTFS格式硬盘
1. 安装驱动
```shell
sudo apt update && sudo apt install ntfs-3g fuse -y
```

2. 创建临时挂载目录
```shell
# 创建挂载点目录文件
sudo mkdir -p /mnt/disk
```

3. 获取硬盘uuid
```shell
# 查看已连接的设备
sudo fdisk -l
# 获取硬盘的UUID
sudo blkid
# 示例: 
# /dev/sda2: LABEL="M-fM-^VM-0M-eM-^JM- M-eM-^MM-7" BLOCK_SIZE="512" UUID="E80499A6049977F0" TYPE="ntfs" PARTLABEL="Basic data partition" PARTUUID="81a8dc88-beea-4dfc-a397-24b13aca3989"
# /dev/sda1: PARTLABEL="Microsoft reserved partition" PARTUUID="d36c9f04-51c9-4a3c-a8cc-4437d11a0864"
```

4. 创建 Udev 规则（热插拔核心）
将 conf目录下 99-automount.rules 文件 移动到 /etc/udev/rules.d/

5. 创建 Systemd 服务（支持启动挂载）
   将 conf目录下 automount@.service 文件 移动到 /etc/systemd/system/

6. 启用服务
```shell
# 重载配置
sudo udevadm control --reload
sudo systemctl daemon-reload

# 启用服务（使启动时生效）
sudo systemctl enable automount@mydrive.service

# 立即测试启动挂载
sudo systemctl start automount@mydrive.service
``` 


