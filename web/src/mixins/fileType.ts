export default function () {
  /**
   *
   * @param fileType 文件类型
   * 文件类型
   */
  const fileTypeEnum = (fileType: any) => {
    let type = { key: 0, value: "" }
    let img = ["jpg", "png"];
    let zip = ["zip", "7z"];
    let video = ["mp4", "m3u8"];
    if (img.indexOf(fileType) != -1) {
      type.key = 1;
      type.value = "图片"
    } else if (zip.indexOf(fileType) != -1) {
      type.key = 2;
      type.value = "压缩文件"
    } else if (video.indexOf(fileType) != -1) {
      type.key = 3;
      type.value = "视频"
    }
    return type
  }

  /**
   *
   * @param fileStatus 0:本地服务器 1:正在同步本地服务器 2:等待同步 3:正在同步远程服务器 4:远程服务器
   */
  const fileStatusEnum = (fileStatus: any) => {
    let status = { key: 0, value: "" }
    if (fileStatus === 0) {
      status.key = 0;
      status.value = "本地服务器";
    }

    return status
  }

  const fileSizeConvert = (fileSize: any) => {
    let status = { size: fileSize, value: "" }
    if (fileSize === null || fileSize === undefined) {
      status.value = "-"
    } else if (fileSize < 1024) {
      status.value = status.size + "B"
    } else if (fileSize >= 1024 && fileSize < 1024 * 1024) {
      status.value = (status.size / 1024).toFixed(1) + "KiB"
    } else if (fileSize >= 1024 * 1024 && fileSize < 1024 * 1024 * 1024) {
      status.value = (status.size / 1024 / 1024).toFixed(1) + "MiB"
    } else if (fileSize >= 1024 * 1024 * 1024 && fileSize < 1024 * 1024 * 1024 * 1024) {
      status.value = (status.size / 1024 / 1024 / 1024).toFixed(1) + "GiB"
    }

    return status;
  }
  return {
    fileTypeEnum,
    fileStatusEnum,
    fileSizeConvert
  }
}
