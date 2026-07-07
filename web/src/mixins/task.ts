export default function () {

  /**
   *
   * @param type 任务状态 1：待执行 2：进行中 3：已结束
   *
   */
  const taskStatus = (type: any) => {
    let val = ''
    switch (type) {
      case '1':
        val = '待执行'
        break
      case '2':
        val = '进行中'
        break
      case '3':
        val = '已结束'
        break
      default:
        val = ''
    }
    return val
  }

   /**
   *
   * @param type 任务触发方式：1：指定时间 2：延时 3：cron表达式
   *
   */
  const taskTrigger = (type: any) => {
    let val = ''
    switch (type) {
      case '1':
        val = '指定时间'
        break
      case '2':
        val = '延时(秒)'
        break
      case '3':
        val = 'Cron表达式'
        break
      default:
        val = ''
    }
    return val
  }



  return {
    taskStatus,
    taskTrigger,
  }
}
