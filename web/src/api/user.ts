import api from "@/api/api"

// --------------角色-------------
// 角色列表接口
export const roleListApi = (pageNum: any, pageSize: any) => {
  const uri = "/auth/role/list?pageNum=" + pageNum + "&pageSize=" + pageSize
  return api.get(uri)
}

// 创建角色接口
export const createRoleApi = (roleDate: any) => {
  const uri = "/user/role/save"
  return api.post(uri, roleDate)
}

// 修改角色接口
export const updateRoleApi = (roleDate: any) => {
  const uri = "/user/role/update"
  return api.post(uri, roleDate)
}

// 删除角色接口
export const deleteRoleApi = (ids: any) => {
  const uri = "/user/role/delete?ids=" + ids
  return api.delete(uri)
}



// 修改角色权限接口
export const updateRolePerApi = (rolePer: any) => {
  const uri = "/user/role/permission/update"
  return api.post(uri, rolePer)
}

// --------------用户-------------

// 用户列表接口
export const userListApi = (page: any, size: any) => {
  const uri = "/user/user/list?page=" + page + "&size=" + size
  return api.get(uri)
}

// 管理员修改用户信息
export const updateUserPerApi = (userDate: any) => {
  const uri = "/user/user/permission/update"
  return api.post(uri, userDate)
}

// 用户修改个人信息
export const updateUserApi = (userDate: any) => {

}

// 用户注册
export const registerUserApi = (userDate: any) => {
  const uri = "/user/user/register"
  return api.put(uri, userDate)
}

// 获取注册验证码
export const getUserVerifyCodeApi = (email: any) => {
  const uri = "/user/user/code?email=" + email
  return api.get(uri)
}


// --------------菜单-------------

/**
 * 获取用户菜单
 * menuType = 1 获取到目录
 * menuType = 2 获取到操作
 * @param menuType
 * @returns
 */
export const userMenuApi = () => {
  const uri = "/user/menu/list"
  return api.get(uri)
}
