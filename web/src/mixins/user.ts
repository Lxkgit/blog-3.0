export default function () {
  /**
   *
   * @param type 用户状态类型
   * 用户状态（true：正常 false：异常）
   */
  const userStatus = (type: any) => {
    let val = ''
    switch (type) {
      case "0":
        val = '禁用'
        break
      case '1':
        val = '正常'
        break
      default:
        val = ''
    }
    return val
  }

  return {
    userStatus,
  }
}
