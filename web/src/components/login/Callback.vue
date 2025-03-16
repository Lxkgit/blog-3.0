<template>

</template>

<script lang="ts" setup>
import { onMounted } from 'vue';
//引入路由
import {useRouter} from 'vue-router'
import { systemStore } from '@/store/system'
import user from "@/utils/user"

const { userTokenFun } = user();
//获取路由器
const router=useRouter();


//根据授权码获取token
const getToken = () => {
  const code = router.currentRoute.value.query.code;
  //获取token，auth是前缀，在vite.config.ts中会去掉这个前缀，为了解决跨域
  userTokenFun(code)
}

onMounted(()=>{
    //获取token 并跳转界面
    getToken()
})



</script>

<style scoped>

</style>
