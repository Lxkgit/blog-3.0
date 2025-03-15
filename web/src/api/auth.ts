import api from "@/api/api"

// // 用户登录接口
// export const userLoginApi = (username: string, password: string) => {
//   const uri = "/auth/oauth/token?grant_type=password&client_id=system&client_secret=system&username=" + username + "&password=" + password
//   return api.post(uri)
// }

// 用户登录接口
export const userLoginApi = (param: any) => {
  const uri = "/auth/doLogin"
  return api.post(uri, param)
}

export const userTokenApi = (param: any) => {
  const uri = "/auth/getToken"
  return api.post(uri, param)
}
