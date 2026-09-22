import api from "@/api/api"

// 上传文件接口
export const uploadApi = (params: any) => {
    const uri = "/file/upload"
    return api.post(uri, params)
}

// 导入日记
export const importDiaryApi = (params: any) => {
    const uri = "/file/upload/diary/import"
    return api.post(uri, params)
}

// 博客数据接口
export const selectBlogDataApi = () => {
    const uri = "/file/data"
    return api.get(uri)
}

// --------------设置接口-------------

export const selectBlogSettingByIdApi = (id: any) => {
    const uri = "/file/setting/id?id=" + id
    return api.get(uri)
}

export const selectBlogSettingApi = (params: any) => {
    const uri = "/file/setting/select"
    return api.get(uri, params)
}

export const updateBlogSettingApi = (params: any) => {
    const uri = "/file/setting/update"
    return api.post(uri, params)
}


// --------------文件服务接口-------------

export const saveFileDirApi = (params: any) => {
    const uri = "/file/dir/save"
    return api.post(uri, params)
}

export const deleteFileDirApi = (params: any) => {
    const uri = "/file/dir/delete"
    return api.delete(uri, params)
}

export const deleteFileApi = (params: any) => {
  const uri = "/file/dir/delete/file"
  return api.delete(uri, params)
}

export const updateFileDirOrFileApi = (params: any) => {
    const uri = "/file/dir/update"
    return api.delete(uri, params)
}

export const selectFileDirApi = (params: any) => {
    const uri = "/file/dir/select"
    return api.get(uri, params)
}

export const selectFileApi = (params: any) => {
  const uri = "/file/dir/select/file"
  return api.get(uri, params)
}

export const syncFileApi = (params: any) => {
    const uri = "/file/dir/sync/file"
    return api.get(uri, params)
}

export const moveFileApi = (params: any) => {
    const uri = "/file/dir/move/file"
    return api.post(uri, params)
}



// --------------服务器设备接口-------------

export const saveDeviceApi = (params: any) => {
    const uri = "/file/device/save"
    return api.post(uri, params)
}

export const deleteDeviceApi = (params: any) => {
    const uri = "/file/device/delete"
    return api.delete(uri, params)
}

export const updateDeviceApi = (params: any) => {
    const uri = "/file/device/update"
    return api.post(uri, params)
}

export const selectDeviceListApi = () => {
    const uri = "/file/device/list"
    return api.get(uri)
}

export const selectDeviceByDeviceIdApi = (params: any) => {
    const uri = "/file/device/id"
    return api.get(uri, params)
}

export const selectDeviceInfoByDeviceCodeApi = (params: any) => {
    const uri = "/file/device/info"
    return api.get(uri, params)
}

// --------------单片机接口-------------

export const saveChipApi = (params: any) => {
    const uri = "/file/chip/save"
    return api.post(uri, params)
}

export const deleteChipApi = (params: any) => {
    const uri = "/file/chip/delete"
    return api.delete(uri, params)
}

export const updateChipApi = (params: any) => {
    const uri = "/file/chip/update"
    return api.post(uri, params)
}

export const selectChipListApi = (params: any) => {
    const uri = "/file/chip/list"
    return api.get(uri, params)
}

export const selectChipByIdApi = (params: any) => {
    const uri = "/file/chip/id"
    return api.get(uri, params)
}

// --------------传感器接口-------------

export const saveSensorApi = (params: any) => {
    const uri = "/file/sensor/save"
    return api.post(uri, params)
}

export const deleteSensorApi = (params: any) => {
    const uri = "/file/sensor/delete"
    return api.delete(uri, params)
}

export const updateSensorApi = (params: any) => {
    const uri = "/file/sensor/update"
    return api.post(uri, params)
}

export const selectSensorListApi = (params: any) => {
    const uri = "/file/sensor/list"
    return api.get(uri, params)
}

export const selectSensorByIdApi = (params: any) => {
    const uri = "/file/sensor/id"
    return api.get(uri, params)
}

// --------------传感器控制接口-------------


export const saveSensorControlApi = (params: any) => {
    const uri = "/file/sensor/control/save"
    return api.post(uri, params)
}

export const deleteSensorControlApi = (params: any) => {
    const uri = "/file/sensor/control/delete"
    return api.delete(uri, params)
}

export const updateSensorControlApi = (params: any) => {
    const uri = "/file/sensor/control/update"
    return api.post(uri, params)
}

export const selectSensorControlListApi = (params: any) => {
    const uri = "/file/sensor/control/list"
    return api.get(uri, params)
}

export const selectSensorControlByIdApi = (id: any) => {
    const uri = "/file/sensor/control/id?id=" + id
    return api.get(uri)
}

export const sendSensorControlApi = (params: any) => {
    const uri = "/file/sensor/control/send"
    return api.get(uri, params)
}

// --------------传感器数据接口-------------

export const selectSensorDataListApi = (params: any) => {
    const uri = "/file/sensor/data"
    return api.get(uri, params)
}

// --------------传感器类型接口-------------

export const selectSensorTypeListApi = () => {
    const uri = "/file/sensor/type/list"
    return api.get(uri)
}

// --------------传感器模板接口-------------

export const selectSensorTemplateByChipOrSensorIdApi = (params: any) => {
    const uri = "/file/sensorTemplate/chipOrSensorId"
    return api.get(uri, params)
}

// --------------定时任务接口-------------

export const selectTaskBaseListApi = () => {
    const uri = "/file/task/select/base/list"
    return api.get(uri)
}

export const selectTaskEntityByIdApi = (params: any) => {
    const uri = "/file/task/select/child/id"
    return api.get(uri, params)
}

export const startTaskApi = (params: any) => {
    const uri = "/file/task/start"
    return api.get(uri, params)
}

// export const selectTaskLogListApi = (params: any) => {
//     const uri = "/file/task/log/select/list"
//     return api.get(uri, params)
// }

// export const selectTaskLogByTaskUUIDApi = (params: any) => {
//     const uri = "/file/task/log/select/id"
//     return api.get(uri, params)
// }


// 保存任务
export const insertTaskApi = (params: any) => {
    const uri = "/file/task/insert"
    return api.post(uri, params)
}

// 删除任务
export const deleteTaskApi = (id: any) => {
    const uri = "/file/task/delete?id=" + id
    return api.delete(uri)
}

// 修改任务
export const updateTaskApi = (params: any) => {
    const uri = "/file/task/update"
    return api.post(uri, params)
}

// 查询任务列表
export const selectTaskListApi = (params: any) => {
    const uri = "/file/task/select/list"
    return api.post(uri, params)
}

// 查询基础任务
export const selectTaskBaseApi = () => {
    const uri = "/file/task/select/base"
    return api.get(uri)
}

// 查询待执行任务列表
export const runningTaskListApi = (params: any) => {
    const uri = "/file/task/select/running"
    return api.get(uri, params)
}

// 查询任务日志列表
export const selectTaskLogListApi = (params: any) => {
    const uri = "/file/task/log/select/list"
    return api.get(uri, params)
}

// 根据uuid查询日志详情
export const selectTaskLogByTaskUuidApi = (params: any) => {
    const uri = "/file/task/log/select/id"
    return api.get(uri, params)
}

// 立即执行任务
export const runningTaskApi = (id: any) => {
    const uri = "/file/task/running?id=" + id
    return api.get(uri)
}

// 取消任务
export const cancelTaskApi = (id: any) => {
    const uri = "/file/task/cancel?id=" + id
    return api.get(uri)
}


// --------------摄像头接口-------------

// 获取摄像头token
export const getCameraTokenApi = (stream: any) => {
    const uri = "/file/camera/token?stream=" + stream
    return api.post(uri)
}

// --------------博客日历接口-------------

// 新增日历记录
export const saveCalendarApi = (params: any) => {
    const uri = "/file/calendar/save"
    return api.post(uri, params)
}

// 根据ID查询日历记录
export const selectCalendarByIdApi = (id: any) => {
    const uri = "/file/calendar/select/id?id=" + id
    return api.get(uri)

}

// 查询指定月份的全部日历记录
// params: { year: 2026, month: 9 }
export const selectCalendarMonthApi = (params: any) => {
    const uri = "/file/calendar/select/month"
    return api.get(uri, params)
}

// 修改日历记录
export const updateCalendarApi = (params: any) => {
    const uri = "/file/calendar/update"
    return api.put(uri, params)
}

// 删除日历记录
export const deleteCalendarApi = (id: any) => {
    const uri = "/file/calendar/delete/" + id
    return api.delete(uri)
}

