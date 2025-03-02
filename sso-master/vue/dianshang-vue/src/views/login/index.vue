<template>

  <div>
    <h1>VUE登陆</h1>
    <button @click="ssoLogin">单点登录</button>

  </div>




</template>

<script lang="ts" setup>
import { onMounted } from 'vue';
//引入路由
import { useRouter } from 'vue-router'
import Cookies from 'js-cookie';
//获取路由器
let router = useRouter();

//单点登陆
let ssoLogin = () => {
  //获取cookie的值
  let rzId = Cookies.get('rzId');
  let target = 'http://auth-server:8084/oauth2/authorize?response_type=code&client_id=dianshang&scope=openid&redirect_uri=http://localhost:3001/callback'
  if (rzId) {
    //如果认证id不为空 带着认证id 
    target = target + '&rzId=' + rzId;
  }
  //先登陆 在跳转到回调界面 获取授权码 
  window.location.href = target
}


</script>

<style scoped></style>