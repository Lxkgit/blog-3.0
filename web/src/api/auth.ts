import api from "@/api/api"

export const publicKeyApi = () => {
  const uri = "/auth/publicKey"
  return api.get(uri)
}

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
export const userMenuApi = (menuType: any) => {
  const uri = "/auth/menu/list/user?menuType=" + menuType
  return api.get(uri)
}

// 查询角色权限列表接口
export const selectRolePerListApi = (roleId: any) => {
  const uri = "/auth/role/permission/select?id=" + roleId
  return api.get(uri)
}

/**
 * 获取全部菜单
 * menuType = 1 获取到目录
 * menuType = 2 获取到操作
 * @param menuType
 * @returns
 */
export const allMenuApi = (menuType: any) => {
  const uri = "/auth/menu/list/all?menuType=" + menuType
  return api.get(uri)
}


