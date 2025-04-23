<template>
  <div class="login-register">
    <div :class="(component === 'Login' ? '' : 'right-panel-active') +
      ' container animate__animated animate__zoomIn'
      ">
      <div class="form-container sign-up-container">
        <div>
          <h1>用户注册</h1>
          <el-form class="registerForm" ref="registerRef" :model="registerForm" label-width="0" :rules="registerRules">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名">
                <template #prefix>
                  <MyIcon type="icon-plus" />
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password1">
              <el-input v-model="registerForm.password1" type="password" placeholder="请输入密码" show-password>
                <template #prefix>
                  <MyIcon type="icon-password" />
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password2">
              <el-input v-model="registerForm.password2" type="password" placeholder="请再次输入密码" show-password>
                <template #prefix>
                  <MyIcon type="icon-password" />
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱号">
                <template #prefix>
                  <MyIcon type="icon-email" />
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <el-input v-model="registerForm.code" placeholder="请输入验证码">
                <template #prefix>
                  <MyIcon type="icon-code" />
                </template>
                <template #suffix>
                  <VerifyCodeBtn :btnDisabled="codeBtnDisabled" @pass="registerPass"></VerifyCodeBtn>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button class="register-btn" type="primary" @click="registerUserFun" round>立即注册</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div class="form-container sign-in-container">
        <div>
          <h1>用户登录</h1>
          <el-form class="loginForm" :model="loginForm" ref="loginRef" label-width="0" :rules="loginRules">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名">
                <template #prefix>
                  <MyIcon type="icon-my" />
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password>
                <template #prefix>
                  <MyIcon type="icon-password" />
                </template>
              </el-input>
            </el-form-item>
            <el-form-item class="login-setting">
              <span class="remember"><el-checkbox v-model="remember" label="保持登录"></el-checkbox></span>
              <span class="forget pointer" @click="router.push('/setPassword')">忘记密码</span>
            </el-form-item>
            <el-form-item class="login-btn">
              <el-button type="primary" @click="loginSubmit" round>立即登录</el-button>
            </el-form-item>
          </el-form>
          <div class="other-login">
            <el-divider>
              <span>第三方账号登录</span>
            </el-divider>
            <div class="other-logo">
              <span @click="otherLogin('QQ')" class="pointer">
                <MyIcon type="icon-qq-logo" />
              </span>
            </div>
          </div>
        </div>
      </div>
      <div class="overlay-container">
        <div class="overlay">
          <div class="overlay-panel overlay-left">
            <div class="point">
              <h1>欢迎回来</h1>
              <p>欢迎注册{{ sitename }}<br />若您已有账号，请切换登录</p>
              <el-button @click="switchLogin" type="danger">切换登录</el-button>
            </div>
          </div>
          <div class="overlay-panel overlay-right">
            <div class="point">
              <h1>欢迎光临</h1>
              <p>欢迎访问{{ sitename }}<br />若您还没有账号，请切换注册</p>
              <el-button @click="switchRegister" type="danger">切换注册</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="LoginRegister" lang="ts">
import icon from '@/utils/icon'
import { onBeforeMount, onMounted, reactive, ref, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { systemStore } from '@/store/system'
import RSAUtil from '@/utils/RSAUtil'
import mitter from '@/utils/mitt'
import user from '@/utils/user'


const { encryptPassword } = RSAUtil()
const { userLoginFun } = user()
const store = systemStore()
const router = useRouter()
let { MyIcon } = icon()

// 引入公共模块
let { switchLogin, switchRegister, bgiURL, component, sitename } = publicFn()
// 引入登录模块
let { loginForm, loginRules, remember, isPassing, verifyPass, btnType, otherLogin } = loginFn()
// 引入注册模块
let { registerForm, registerRules, codeBtnDisabled, registerPass, registerUserFun } = registerFn()

const loginRef: any = ref(null)
// 登录表单提交事件
const loginSubmit = async () => {
  if (loginRef.value !== null) {
    let password = await encryptPassword(loginForm.password)
    loginRef.value.validate((valid: any) => {
      if (valid && isPassing.value) {
        userLoginFun({
          username: loginForm.username,
          password: password,
        })
      } else {
        btnType.value = 'danger'
        ElMessage.error('请检查表单内容后再登录')
        return false
      }
    })
  }
}

// 公共模块
function publicFn() {
  // 背景图片地址
  const bgiURL = ref('')
  // 当前组件名称
  const component: any = ref('Login')

  // 站点名称
  const sitename = ref('GSZero个人小站')

  // 切换登录页事件
  const switchLogin = () => {
    component.value = 'Login'
  }
  // 切换注册页事件
  const switchRegister = () => {
    component.value = 'Register'
  }
  onBeforeMount(() => {

  })
  // 其他页面调用，默认跳转
  onMounted(() => {
 
  })
  return { switchLogin, switchRegister, bgiURL, component, sitename }
}

// 登录模块
function loginFn(): any {
  // 登录表单
  const loginForm = reactive({
    username: '',
    password: '',
  })
  // 登录表单验证规则
  const loginRules = {
    username: [
      {
        required: true,
        message: '请输入用户名/邮箱号/手机号',
        trigger: 'blur',
      },
    ],
    password: [
      {
        required: true,
        message: '请输入密码',
        trigger: 'blur',
      },
    ],
  }
  // 是否记住密码
  const remember = ref(false)
  // 滑块验证是否通过
  const isPassing = ref(true)
  // 滑块验证按钮样式
  const btnType = ref('default')
  // 滑块验证通过事件
  const verifyPass = () => {
    console.log('验证通过了')
    isPassing.value = true
  }
  // 第三方登录
  const otherLogin = (kind: any) => { }
  return { loginForm, remember, isPassing, verifyPass, btnType, otherLogin, loginRules }
}

// 注册模块
function registerFn() {
  // 注册表单
  const registerForm = reactive({
    username: '',
    email: '',
    code: '',
    password: '',
    password1: '',
    password2: '',
  })
  // 用户名验证
  const checkUsername = (rule: any, value: any, callback: any) => {
    if (!value) {
      return callback(new Error('请输入用户名'))
    }
    setTimeout(() => { }, 500)
  }
  // 联系方式验证
  const checkEmail = (rule: any, value: any, callback: any) => {
    if (!value) {
      return callback(new Error('请输入邮箱号'))
    }
    setTimeout(() => {
      registerForm.email = value
      const pattern = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
      if (!pattern.test(value)) {
        codeBtnDisabled.value = true
        callback(new Error('请输入正确的邮箱'))
      } else {
        codeBtnDisabled.value = false
        callback()
      }
    }, 500)
  }
  // 用户密码验证
  const checkPassword1 = (rule: any, value: any, callback: any) => {
    if (!value) {
      return callback(new Error('请输入密码'))
    }
    setTimeout(() => {
      registerForm.password1 = value
      const pattern = /^[0-9A-Za-z]{5,16}$/
      if (!pattern.test(value)) {
        callback(new Error('密码必须是数字或字母，5-16位长度！'))
      } else {
        callback()
      }
    }, 500)
  }
  // 用户确认密码验证
  const checkPassword2 = (rule: any, value: any, callback: any) => {
    if (!value) {
      return callback(new Error('请再次输入密码'))
    }
    setTimeout(() => {
      registerForm.password2 = value
      const pattern = /^[0-9A-Za-z]{5,16}$/
      if (!pattern.test(value)) {
        callback(new Error('密码必须是数字或字母，5-16位长度！'))
      } else {
        if (registerForm.password1 !== registerForm.password2) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      }
    }, 500)
  }
  // 注册表单验证规则
  const registerRules = {
    username: [{ validator: checkUsername, trigger: 'blur' }],
    email: [{ validator: checkEmail, trigger: 'blur' }],
    code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
    password1: [{ validator: checkPassword1, trigger: 'blur' }],
    password2: [{ validator: checkPassword2, trigger: 'blur' }],
  }
  // 获取验证码表单
  const codeForm = reactive({
    contact: '',
    action: '用户注册',
    username: '新用户',
  })
  // 获取验证码按钮默认状态
  const codeBtnDisabled = ref(true)
  // 获取注册验证码通过事件
  const registerPass = () => {

  }
  // 注册表单提交事件
  const registerUserFun = () => {
    // registerUserApi({
    //     username: registerForm.username,
    //     password: registerForm.password1,
    //     email: registerForm.email,
    //     code: registerForm.code
    // }).then((res: any) => {
    //     if(res.code === 200) {
    //       ElMessage({
    //       message: '用户注册成功',
    //       type: 'success',
    //     })
    //     }
    // })
  }
  return { registerForm, codeBtnDisabled, registerRules, registerPass, registerUserFun }
}
</script>

<style scoped>
.login-register {
  background-image: v-bind('bgiURL');
  width: 100vw;
  height: 100vh;
  background-size: 100% 100%;
  background-attachment: fixed;
  display: block;
  position: relative;
}

.login-register .container {
  border-radius: 10px;
  box-shadow:
    0 14px 28px rgba(0, 0, 0, 0.25),
    0 10px 10px rgba(0, 0, 0, 0.22);
  position: absolute;
  overflow: hidden;
  width: 768px;
  max-width: 100%;
  min-height: 520px;
  opacity: 0.9;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
}

.login-register .container .form-container {
  position: absolute;
  top: 0;
  height: 100%;
  transition: all 0.6s ease-in-out;
}

.login-register .container .form-container>div {
  background: #2d3436;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.login-register .container .form-container>div h1 {
  color: var(--el-color-primary);
  margin-bottom: 30px;
}

.login-register .container .form-container>div .loginForm {
  width: 300px;
}

.login-register .container .form-container>div .loginForm .login-setting {
  color: var(--el-text-color-secondary);
}

.login-register .container .form-container>div .loginForm .login-setting .remember {
  float: left;
}

.login-register .container .form-container>div .loginForm .login-setting .remember .el-checkbox {
  color: var(--el-text-color-secondary);
}

.login-register .container .form-container>div .loginForm .login-setting .forget {
  float: right;
  margin-left: 165px;
}

.login-register .container .form-container>div .loginForm .login-btn button {
  margin: 0 auto;
}

.login-register .container .form-container>div .other-login {
  display: contents;
}

.login-register .container .form-container>div .other-login .el-divider {
  background-color: var(--el-border-color);
}

.login-register .container .form-container>div .other-login .other-logo span {
  font-size: 35px;
  margin: 0 7px;
  opacity: 1;
}

.login-register .container .form-container>div .registerForm {
  width: 300px;
}

.login-register .container .sign-in-container {
  left: 0;
  width: 50%;
  z-index: 2;
}

.login-register .container .sign-up-container {
  left: 0;
  width: 50%;
  z-index: 1;
  opacity: 0;
}

.login-register .container .overlay-container {
  position: absolute;
  top: 0;
  left: 50%;
  width: 50%;
  height: 100%;
  overflow: hidden;
  transition: transform 0.6s ease-in-out;
  z-index: 100;
}

.login-register .container .point h1 {
  margin-bottom: 30px;
  color: var(--el-color-primary);
  opacity: 1;
}

.login-register .container .point p {
  margin-bottom: 50px;
  line-height: 30px;
  padding: 10px;
  color: var(--el-color-primary);
  font-weight: bolder;
  font-size: 18px;
}

.login-register::after {
  content: '';
  opacity: 0.9;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  position: absolute;
  z-index: -1;
  filter: blur(1px);
}

.overlay {
  background: transparent;
  color: #fff;
  position: absolute;
  left: -100%;
  height: 100%;
  width: 200%;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
}

.overlay .overlay-panel {
  position: absolute;
  top: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 50%;
  text-align: center;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
}

.overlay .overlay-right {
  right: 0;
  transform: translateX(0);
  background-color: rgba(255, 255, 255, 0.5);
}

.overlay .overlay-left {
  transform: translateX(0);
  background-color: rgba(255, 255, 255, 0.5);
}

.container.right-panel-active .sign-in-container {
  transform: translateX(100%);
}

/*....Move overlay to the left....*/
.container.right-panel-active .overlay-container {
  transform: translateX(-100%);
}

/*....Bring sign up over sign in....*/
.container.right-panel-active .sign-up-container {
  transform: translateX(100%);
  opacity: 1;
  z-index: 5;
}

/*...Move overlay back to right....*/
.container.right-panel-active .overlay {
  transform: translateX(50%);
}

.container.right-panel-active .overlay-left {
  transform: translateX(0);
}

.container.right-panel-active .overlay-right {
  transform: translateX(0);
}

.register-btn {
  margin: 20px auto;
}
</style>
