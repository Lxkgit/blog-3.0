import { computed, onMounted, ref, onActivated, watch } from "vue";
import { systemStore } from "@/store/system"
import { ElMessage } from 'element-plus'
import { useRouter } from "vue-router";
import { userLoginApi, userTokenApi, userInfoApi, userLogoutApi, userMenuApi } from "@/api/auth"



function user() {
	// let { openSocketUser, closeWebSocketUser } = socketUser()
	const store = systemStore()
	let isLogin = ref(false)
	const userId = ref()
	const userToken = ref()
	const userName = ref()
	const router = useRouter()

	onActivated(() => {

	})

	onMounted(() => {
      console.log(" -- -" + store.isLogin)
      isLogin.value = store.isLogin
	})

  const userLoginFun = (param: any) => {
    userLoginApi(param).then((res: any) => {
        ElMessage({
          message: '登录成功！',
          type: 'success',
        })
        store.userLocal.rz_id = res.result
        //从路由拿到参数
        const target = router.currentRoute.value.query.target;
        window.location.href = target + "&rzId=" + res.result;
      }).catch((res) => {
        //发生错误时执行的代码
        console.log(res)
        ElMessage.error('账号或密码错误！')
      })
  }

  const userTokenFun = (code: any) => {
    userTokenApi(
      {
        //授权码
        code: code,
        //回调地址
        redirectUri: 'http://localhost:5173/callback',
        //客户端id
        clientId: 'dianshang',
        //客户端密码
        clientSecret: '123456',
      }
    ).then((res: any) => {
      //把token放入Cookie中
      store.userLocal.access_token = res.result.access_token

      store.isLogin = true
      userInfoFun()
      //然后跳转到首页
      router.push('/admin/index')
    })
  }

  const userInfoFun = () => {
    userInfoApi().then((res: any) => {
      store.userInfo = res.result
      console.log("userInfo: " + res)
    })
  }



  // 个人中心-退出登录
	const userLogoutFun = () => {
    userLogoutApi({}, { headers: {'rzId': store.userLocal.rz_id}}).then((res: any) => {
      if(res.code === 200) {
        router.push('/home')
      }
    })
	}

	return {
		isLogin, userId, userToken, userName, userLoginFun, userTokenFun, userLogoutFun
	}
}

export default user
