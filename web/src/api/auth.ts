import api from "@/api/api"

// 用户登录接口
export const userLoginApi = (param: any) => {
  const uri = "/auth/doLogin"
  return api.post(uri, param)
}

export const userTokenApi = (param: any) => {
  const uri = "/auth/getToken"
  return api.post(uri, param)
}

// 获取登陆用户信息
export const userInfoApi = (param?: any, headers?: any) => {
  const uri = "/auth/getUser"
  return api.get(uri, param, headers)
}

// 退出
export const userLogoutApi = (param?: any, headers?: any) => {
  const uri = "/auth/tuiChu"
  return api.post(uri, param, headers)
}

// 获取用户菜单
export const userMenuApi = (param: any) => {
  const uri = "/auth/menu/user"
  return api.post(uri, param)
}
