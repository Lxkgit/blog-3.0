<template>
    <div>
                <div>
                    <h1>库存系统</h1>
                    <!--显示用户信息-->
                    当前登录人:{{ name }}
                    <button @click="logout">退出登录</button>
                </div>

                <div>
                  库存接口内容:{{ kcTest }}
                  
                </div>
                <div>
                        <!--显示菜单点击后的内容-->
                        <router-view></router-view>
                </div>
    </div>    
 
   
  </template>
  
  <script lang="ts" setup>
  import { onMounted,ref } from 'vue';
  //引入路由
  import {useRouter} from 'vue-router'
  import axios from "axios";
  import Cookies from 'js-cookie';
  //获取路由器
  let router=useRouter();
 
  //定义当前用户字段
  let name=ref('')
  //定义库存cc接口内容
  let kcTest=ref('')
 

  //获取用户信息
  let getUserInfo=()=>{
    //获取token
   // let token=localStorage.getItem('TOKEN')
    let token=Cookies.get('TOKEN')
    if(token){
        //拿着token，放到header头中 获取用户信息
        let url="/res/getUser";
        axios.get(url,
        {
          headers: {
              //注意Bearer后面有一个空格
              'authorization': 'Bearer '+token
            }
        })
        .then(function (response) {
            //获取当前用户 并赋值
            let res=response.data.data;
            name.value=res;
        })
        .catch(function (error) {
           // console.log('bb',error);
            //获取异常跳转到登陆界面
            router.push('/login')
        });

    }else{
      //如果没有token 跳转到登陆界面
      router.push('/login')
    }
  }
 
   //获取库存test接口
   let getKcTest=()=>{
    //获取token
   // let token=localStorage.getItem('TOKEN')
    let token=Cookies.get('TOKEN')
    if(token){
        //拿着token，放到header头中 获取用户信息
        let url="/kc/test";
        axios.get(url,
        {
          headers: {
              //注意Bearer后面有一个空格
              'authorization': 'Bearer '+token
            }
        })
        .then(function (response) {
             //获取数据并赋值
            let res=response.data;
            kcTest.value=res.data;
        })
        .catch(function (error) {
           // console.log('bb',error);
            //获取异常跳转到登陆界面
            router.push('/login')
        });

    }else{
      //如果没有token 跳转到登陆界面
      router.push('/login')
    }
  }

  
  //生命周期挂载
  onMounted(()=>{
        //获取用户信息
        getUserInfo();
        //获取库存cc接口
        getKcTest();
        
  })

  

   
//退出登录
let logout=()=>{
  //  let token=localStorage.getItem('TOKEN')
    let token=Cookies.get('TOKEN')
  //获取cookie的值
    let rzId = Cookies.get('rzId');
    if(token){
       
        let url="/auth/tuiChu";
        axios.post(url,{},{
          headers:{
            'rzId':rzId
          }
        })
        .then(function (response) {
             if(response.data.code=='200'){
                //成功后 清除 本地缓存
                //localStorage.removeItem('TOKEN');
                //清除cookie
                Cookies.remove('TOKEN');
                Cookies.remove('rzId');
                
                //跳转登陆界面
                router.push('/login')
             }else{
                alert(response.data.msg);
             }
             
        })
        .catch(function (error) {
            //console.log('bb',error);
            //获取异常跳转到登陆界面
            router.push('/login')
        });

    }else{
      //如果没有token 跳转到登陆界面
      router.push('/login')
    }
}
  </script>
 
  <style scoped>
 
  </style>