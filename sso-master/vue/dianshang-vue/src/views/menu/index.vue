<template>
  <div>
    <div>
      <h1>资源页面</h1>
      <!--显示用户信息-->
      当前登录人:{{ name }}
      <button @click="logout">退出登录</button>
    </div>

    <div>
      无权限接口内容:{{ dsCc0 }}
    </div>
    <div>
      有权限接口内容:{{ dsCc1 }}
    </div>
    <div>
      auth有权限接口内容:{{ dsCc }}
    </div>
    <div>
      <!--显示菜单点击后的内容-->
      <router-view></router-view>
    </div>
  </div>


</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue';
//引入路由
import { useRouter } from 'vue-router'
import axios from "axios";
import Cookies from 'js-cookie';

//获取路由器
let router = useRouter();

//定义当前用户字段
let name = ref('')
let dsCc = ref('')
//定义电商cc接口内容
let dsCc0 = ref('')
//定义电商cc接口内容
let dsCc1 = ref('')

//获取用户信息
let getUserInfo = () => {
  //获取token
  //let token=localStorage.getItem('TOKEN')
  let token = Cookies.get('TOKEN')

  //获取cookie的值
  let rzId = Cookies.get('rzId');

  if (token) {
    //拿着token，放到header头中 获取用户信息
    let url = "/res/auth/getUser";
    axios.get(url,
      {
        headers: {
          //注意Bearer后面有一个空格
          'authorization': 'Bearer ' + token,
          'rzId': rzId
        }
      })
      .then(function (response) {
        //获取当前用户 并赋值
        let res = response.data.result;
        name.value = res;
      })
      .catch(function (error) {
        //console.log('bb',error);
        //获取异常跳转到登陆界面
        // router.push('/login')
      });

  } else {
    //如果没有token 跳转到登陆界面
    router.push('/home')
  }
}

//获取电商cc接口
let getauth = () => {
  //获取token
  //let token=localStorage.getItem('TOKEN')
  let token = Cookies.get('TOKEN')
  if (token) {
    //拿着token，放到header头中 获取用户信息
    let url = "/res/auth/hello1";
    axios.get(url)
      .then(function (response) {
        // alert("资源接口：" + JSON.stringify(response))
        //获取数据并赋值
        let res = response.data;
        dsCc.value = res;
      })
      .catch(function (error) {
        // alert("资源接口报错：" + JSON.stringify(error))
        //console.log('bb',error);
        //获取异常跳转到登陆界面
        // router.push('/login')
      });

  } else {
    //如果没有token 跳转到登陆界面
    router.push('/home')
  }
}

//获取电商cc接口
let getCc0 = () => {
  //获取token
  //let token=localStorage.getItem('TOKEN')
  let token = Cookies.get('TOKEN')
  if (token) {
    //拿着token，放到header头中 获取用户信息
    let url = "/res/content/hello";
    axios.get(url)
      .then(function (response) {
        // alert("资源接口：" + JSON.stringify(response))
        //获取数据并赋值
        let res = response.data;
        dsCc0.value = res;
      })
      .catch(function (error) {
        // alert("资源接口报错：" + JSON.stringify(error))
        //console.log('bb',error);
        //获取异常跳转到登陆界面
        // router.push('/login')
      });

  } else {
    //如果没有token 跳转到登陆界面
    router.push('/home')
  }
}

//获取电商cc接口
let getCc1 = () => {
  //获取token
  //let token=localStorage.getItem('TOKEN')
  let token = Cookies.get('TOKEN')
  if (token) {
    //拿着token，放到header头中 获取用户信息
    let url = "/res/content/hello1";
    axios.get(url,
      {
        headers: {
          //注意Bearer后面有一个空格
          'authorization': 'Bearer ' + token
        }
      })
      .then(function (response) {
        // alert("资源接口：" + JSON.stringify(response))
        //获取数据并赋值
        let res = response.data;
        dsCc1.value = res;
      })
      .catch(function (error) {
        // alert("资源接口报错：" + JSON.stringify(error))
        //console.log('bb',error);
        //获取异常跳转到登陆界面
        // router.push('/login')
      });

  } else {
    //如果没有token 跳转到登陆界面
    router.push('/home')
  }
}


//生命周期挂载
onMounted(() => {
  //获取用户信息
  getUserInfo();
  //获取电商cc接口
  getauth();
  getCc0();
  getCc1();

})


//退出登录
let logout = () => {
  //let token=localStorage.getItem('TOKEN')
  let token = Cookies.get('TOKEN')
  //获取cookie的值
  let rzId = Cookies.get('rzId');
  if (token) {

    let url = "/res/auth/tuiChu";
    axios.post(url, {}, {
      headers: {
        'rzId': rzId
      }
    })
      .then(function (response) {
        // alert(JSON.stringify(response))
        if (response.data.code == '200') {
          //成功后 清除 本地缓存
          // localStorage.removeItem('TOKEN');
          //清除cookie
          Cookies.remove('TOKEN');
          Cookies.remove('rzId');

          //跳转登陆界面
          router.push('/home')
        } else {
          alert(response.data.msg);
        }

      })
      .catch(function (error) {
        //console.log('bb',error);
        //获取异常跳转到登陆界面
        router.push('/home')
      });

  } else {
    //如果没有token 跳转到登陆界面
    router.push('/home')
  }
}

</script>

<style scoped></style>