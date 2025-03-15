<template>
   
</template>

<script lang="ts" setup>
import { onMounted } from 'vue';
import axios from "axios";
//引入路由
import {useRouter} from 'vue-router'
import Cookies from 'js-cookie';
//获取路由器
let router=useRouter();

//根据授权码获取token
let getToken=()=>{
    let code = router.currentRoute.value.query.code;
    if(code){
        //获取token，auth是前缀，在vite.config.ts中会去掉这个前缀，为了解决跨域
        let url="/res/auth/getToken";
        axios.post(url,
        {
            //授权码
            "code":code,
            //回调地址
            "redirectUri":"http://localhost:5173/callback",
            //客户端id
            "clientId":"dianshang",
            //客户端密码
            "clientSecret":"123456"
        })
        .then(function (response) {
            console.log('aa:',response);
            let res=response.data;

            let data=res.result;
            let code=res.code;  
            alert("认证回调：" + JSON.stringify(res));
            if(code=='200'){
        
                let accessToken=data.access_token;
                //把token放入本地缓存中
                //localStorage.setItem('TOKEN',accessToken);
                //把token放入Cookie中
                Cookies.set('TOKEN', accessToken);
                //然后跳转到首页
                router.push('/')
            }else{
               //如果获取失败 跳转到登陆界面 重新登陆
               router.push('/login')
            }
           
        })
        .catch(function (error) {
            console.log('bb',error);
        });

    }
}

onMounted(()=>{
    //获取token 并跳转界面
    getToken()
})



</script>

<style scoped>

</style>