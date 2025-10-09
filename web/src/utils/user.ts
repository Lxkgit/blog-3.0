import { computed, onMounted, ref, onActivated, watch } from 'vue'
import { systemStore } from '@/store/system'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { userLoginApi, userTokenApi, userInfoApi, userLogoutApi, userMenuApi } from '@/api/auth'

function user() {
  // let { openSocketUser, closeWebSocketUser } = socketUser()
  const store = systemStore()
  let isLogin = ref(false)
  let userId = ref()
  let userToken = ref()
  let userName = ref()
  const router = useRouter()

  onActivated(() => {})

  onMounted(() => {
    isLogin.value = store.isLogin
    userId.value = store.userSession.user_id
  })

  const userLoginFun = (param: any) => {
    userLoginApi(param)
      .then((res: any) => {
        ElMessage({ message: '登录成功！', type: 'success' })
        store.userLocal.username = param.username
        store.userLocal.password = param.password
        store.userSession.rz_id = res.result
        //从路由拿到参数
        const target = router.currentRoute.value.query.target
        window.location.href = target + '&rzId=' + res.result
      })
      .catch((res) => {
        //发生错误时执行的代码
        ElMessage.error('账号或密码错误！')
      })
  }

  const userTokenFun = (code: any) => {
    userTokenApi({
      // 授权码
      code: code,
      // 回调地址
      redirectUri: store.callback.callbackUrl,
      // 客户端id
      clientId: 'dianshang',
      // 客户端密码
      clientSecret: '123456',
      // 授权码获取token
      grantType: 'authorization_code',
      username: store.userLocal.username,
    }).then((res: any) => {
      //把token放入Cookie中
      store.userSession.access_token = res.result.access_token
      store.userSession.user_id = res.result.user_id
      store.userLocal.refresh_token = res.result.refresh_token
      ;(store.userMessage.userId = res.result.user_id),
        (store.userMessage.username = store.userLocal.username)
      store.isLogin = true
      userInfoFun()
      //然后跳转到首页
      router.push('/admin/index')
    })
  }

  const refreshTokenFun = async (): Promise<boolean> => {
    if (isLogin.value) {
      return true // 已登录
    }

    const refreshToken = store.userLocal.refresh_token
    if (!refreshToken) {
      return false // 没有 refresh_token
    }

    try {
      const res: any = await userTokenApi({
        clientId: 'dianshang',
        clientSecret: '123456',
        grantType: 'refresh_token',
        username: store.userLocal.username,
        password: store.userLocal.password,
        refreshToken,
      })

      if (res.code === 200) {
        store.userSession.access_token = res.result.access_token
        store.userSession.rz_id = res.result.rz_id
        store.userSession.user_id = res.result.user_id
        store.userLocal.refresh_token = res.result.refresh_token
        store.isLogin = true
        userInfoFun()
        location.reload()
        return true
      } else {
        localStorage.clear()
        return false
      }
    } catch (err) {
      return false
    }
  }

  const userInfoFun = () => {
    userInfoApi().then((res: any) => {
      store.userInfo = res.result
    })
  }

  // 个人中心-退出登录
  const userLogoutFun = () => {
    store.isLogin = false
    store.userSession = {
      rz_id: '',
      user_id: '',
      access_token: '',
    }
    store.userLocal = {
      refresh_token: '',
      username: '',
      password: '',
    }

    router.push('/')
  }

  return {
    isLogin,
    userId,
    userToken,
    userName,
    userLoginFun,
    refreshTokenFun,
    userTokenFun,
    userLogoutFun,
  }
}

export default user
