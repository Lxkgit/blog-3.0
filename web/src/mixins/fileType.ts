export default function () {
  /**
   *
   * @param fileType 文件类型
   * 文件类型
   */
  const fileType = (fileType: any) => {
    let type = {
      key: 0,
      value: ""
    }
    let img = ["jpg", "png"];
    let zip = ["zip", "7z"];
    if(img.indexOf(fileType) != -1) {
      type.key = 1;
      type.value = "图片"
    } else if (zip.indexOf(fileType) != -1) {
      type.key = 2;
      type.value = "压缩文件"
    }



    return type
  }

  return {
    fileType,
  }
}
