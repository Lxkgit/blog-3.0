<template>

</template>

<script lang="ts" setup>
import { onMounted } from 'vue';
import axios from "axios";
//引入路由
import {useRouter} from 'vue-router'
import { systemStore } from '@/store/system'
import { userTokenApi } from '@/api/auth'
//获取路由器
let router=useRouter();
const store = systemStore()

//根据授权码获取token
const getToken = () => {
  let code = router.currentRoute.value.query.code;
  //获取token，auth是前缀，在vite.config.ts中会去掉这个前缀，为了解决跨域
  userTokenApi({
    //授权码
    code: code,
    //回调地址
    redirectUri: 'http://localhost:5173/callback',
    //客户端id
    clientId: 'dianshang',
    //客户端密码
    clientSecret: '123456',
  }).then((res: any) => {
      // alert('认证回调：' + JSON.stringify(res))
      //把token放入Cookie中
      store.setUserLocal({access_token: res.result.access_token})
      //然后跳转到首页
      router.push('/')
    })
    .catch((res) => {
      alert('认证回调异常' + JSON.parse(res))
    })
}

onMounted(()=>{
    //获取token 并跳转界面
    getToken()
})



</script>

<style scoped>

</style>
