
<template>
  <div>
    <!--显示路由-->
    <!-- <router-view></router-view> -->
  </div>
  <h1>统一认证平台</h1>

  <div>
    账号:<input type="text" v-model="username"/>
    密码:<input type="text" v-model="password"/>
    <button @click="dengLu">登陆</button>
  </div>
 
</template>
<script setup lang="ts">
import axios from 'axios';
import {queuePostFlushCb, ref} from 'vue'
//引入路由
import {useRouter} from 'vue-router'
import Cookies from 'js-cookie'
//获取路由器
let router=useRouter();
//定义字段
let username=ref('')
let password=ref('')

//登陆方法
let dengLu=()=>{
  //从路由拿到参数
  let target=router.currentRoute.value.query.target;
        //调用认证授权服务器的登陆接口
        //http://auth-server:8084/doLogin  
        let url="/auth/doLogin";
        axios.post(url,
        {
          username:username.value,
          password:password.value
         }
        )
        .then(function (response) {
            // console.log('login:',response)
             let code=response.data.code;
             let rzId=response.data.data;
             if(code=='200'){
                //把rzId放入cookie 多个客户端共享
                Cookies.set('rzId', rzId);
                //跳转授权地址
                window.location.href=target+"&rzId="+rzId;
             }else{
               //弹出错误提示
               alert(response.data.msg);
             }
        })
        .catch(function (error) {
            console.log('bb',error);
            //获取异常跳转到登陆界面
            //router.push('/login')
        });


}



</script>

