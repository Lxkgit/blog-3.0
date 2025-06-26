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
      result = "-"
    } else if (seconds < 60) {
      return Math.floor(seconds) + "秒"
    } else if (seconds >= 60 && seconds < 60 * 60) {
      return Math.floor(seconds / 60) + "分" + Math.floor(seconds % 60) + "秒"
    } else if (seconds >= 60 * 60) {
      return Math.floor(seconds / 3600) + "小时" + Math.floor(seconds % 3600 / 60) + "分" + Math.floor(seconds % 60) + "秒"
    }
    return result;
  }

  return {
    timeAgo,
    timeMonth,
    timeDate,
    timeFull,
    timeFile,
    timeToMinOrHour
  }
}

export default timeFormat

