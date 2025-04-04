export default function () {
  /**
   *
   * @param type 设备在离线状态
   *
   */
  const deviceStatus = (type: any) => {
    let val = ''
    switch (type) {
      case 0:
        val = '离线'
        break
      case 1:
        val = '在线'
        break
      default:
        val = ''
    }
    return val
  }

  return {
    deviceStatus,
  }
}
