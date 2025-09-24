// 日期对象格式化转换
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn' // import locale
dayjs.extend(relativeTime)
dayjs.locale('zh-cn') // use locale

function timeFormat() {
  // 显示几天前
  const timeAgo = (valueTime: any) => {
    return dayjs(valueTime).fromNow()
  }
  // 只显示年月
  const timeMonth = (valueTime: any) => {
    return dayjs(valueTime).format('YYYY-MM')
  }
  // 只显示年月日
  const timeDate = (valueTime: any) => {
    return dayjs(valueTime).format('YYYY-MM-DD')
  }
  // 显示全部时间
  const timeFull = (valueTime: any) => {
    return dayjs(valueTime).format('YYYY-MM-DD HH:mm:ss')
  }
  // 时间_用于文件名
  const timeFile = (valueTime: any) => {
    return dayjs(valueTime).format('YYYY_MM_DD_HH_mm_ss_SSS')
  }

  const timeToMinOrHour = (seconds: any) => {
    let result: any
    if (seconds === null || seconds === undefined) {
      result = '-'
    } else if (seconds < 60) {
      return Math.floor(seconds) + '秒'
    } else if (seconds >= 60 && seconds < 60 * 60) {
      return Math.floor(seconds / 60) + '分' + Math.floor(seconds % 60) + '秒'
    } else if (seconds >= 60 * 60) {
      return (
        Math.floor(seconds / 3600) +
        '小时' +
        Math.floor((seconds % 3600) / 60) +
        '分' +
        Math.floor(seconds % 60) +
        '秒'
      )
    }
    return result
  }

  /**
   * 计算两个时间的差值并返回格式化后的字符串
   * @param start 起始时间（any）
   * @param end 结束时间（any）
   * @returns 格式化字符串，如 "1天 2小时 3分 4秒"
   */
  const diffFormat = (start: any, end: any) => {
    const startTime = toTimestamp(start)
    const endTime = toTimestamp(end)

    let diff = Math.abs(endTime - startTime)

    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    diff %= 1000 * 60 * 60 * 24

    const hours = Math.floor(diff / (1000 * 60 * 60))
    diff %= 1000 * 60 * 60

    const minutes = Math.floor(diff / (1000 * 60))
    diff %= 1000 * 60

    const seconds = Math.floor(diff / 1000)

    // 拼接字符串，自动省略为 0 的单位
    const parts: string[] = []
    if (days > 0) parts.push(`${days}天`)
    if (hours > 0) parts.push(`${hours}小时`)
    if (minutes > 0) parts.push(`${minutes}分`)
    if (seconds > 0 || parts.length === 0) parts.push(`${seconds}秒`)

    return parts.join('')
  }

  const toTimestamp = (time: any) => {
    if (time instanceof Date) return time.getTime();
    if (typeof time === "number") return time;
    if (typeof time === "string") {
      const t = new Date(time).getTime();
      if (!isNaN(t)) return t;
    }
    throw new Error(`Invalid time input: ${time}`);
  }

  return {
    timeAgo,
    timeMonth,
    timeDate,
    timeFull,
    timeFile,
    timeToMinOrHour,
    diffFormat,
  }
}

export default timeFormat
