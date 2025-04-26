import { defineStore } from "pinia";

export const systemStore = defineStore('system', () => {

  // markdown目录(sessionStorage)
  let outline = {}

  // markdown目录是否显示
  let outlineShow = true

  // 登录后跳转页
  let nextPath = '/admin'

  // 是否保持登录
  let keepLogin: false

  // 保持登录用户信息(localStorage)
  let userLocal = {
      refresh_token: "",
      username: "",
      password: ""
  }

  let userInfo = {}

  // 临时登录用户信息(sessionStorage)
  let userSession = {
      rz_id: "",
      user_id: "",
      access_token: ""
  }

  // 个人中心导航栏是否折叠
  let asideMenuFold = false

  // 默认主题色
  let theme = '#409eff'

  // 导航栏样式
  let navigation = 'auto'

  // 当前激活的导航栏菜单id
  let menuIndex = "1"

  // 是否开启深色模式
  let isDark = false

  // 用户是否登录
  let isLogin = false

  let sideBar = false

  // 是否获取过socket信息
  let socketFlag = false

  // 全局socket
  let globalSocket = false

  // 用户socket
  let userSocket = false

  // 服务器IP
  let serviceIP =  ""

  // 设置markdown目录内容
  function setOutline(value: any) {
    this.outline = value
  }

  // 设置markdown是否显示
  function setOutlineShow() {
    this.outlineShow = !this.outlineShow
  }

  // 设置登录后跳转的地址
  function setNextPath(path: any) {
    this.nextPath = path
  }

  // 是否保持登录
  function setKeepLogin(value: any) {
    this.keepLogin = value
  }

  // 用户信息（保持登录）
  function setUserLocal(value: any) {
    this.userLocal = value
  }

  // 用户信息（临时存储）
  function setUserSession(value: any) {
    this.userSession = value
  }

  // 个人中心导航栏是否折叠
  function setAsideMenuFold(value: any) {
    this.asideMenuFold = value
  }

  // 设置主题色
  function setTheme(value: any) {
    this.theme = value
  }

  // 设置导航栏模式
  function setNavigation(value: any) {
    this.navigation = value
  }

  // 设置导航栏当前激活的菜单id
  function setMenuIndex(value: any) {
    this.menuIndex = value
  }

  // 设置深色模式
  function setDark(value: any) {
    this.isDark = value
  }

  function setSocketFlag(value: any) {
    this.socketFlag = value
  }

  // 设置全局socket是否开启
  function setGlobalSocket(value: any) {
    this.globalSocket = value
  }

  // 设置用户socket是否开启
  function setUserSocket(value: any) {
    this.userSocket = value
  }

  // 设置服务器IP
  function setServiceIP(value: any) {
    this.serviceIP = value
  }

  return {
    outline,
    outlineShow,
    nextPath,
    keepLogin,
    userLocal,
    userInfo,
    userSession,
    asideMenuFold,
    theme,
    navigation,
    menuIndex,
    isDark,
    isLogin,
    sideBar,
    socketFlag ,
    globalSocket,
    userSocket,
    serviceIP ,

    setOutline,
    setOutlineShow,
    setNextPath,
    setKeepLogin,
    setUserLocal,
    setUserSession,
    setAsideMenuFold,
    setTheme,
    setNavigation,
    setMenuIndex,
    setDark,
    setSocketFlag,
    setGlobalSocket,
    setUserSocket,
    setServiceIP
  }

}, {
  // persist: true,
  persist:  {
    storage: localStorage
  }

  // persist:  [
  //   {
  //     pick: ['userLocal'],
  //     storage: localStorage,
  //     key: 'userLocal'
  //   },
  //   {
  //     pick: ['userSession'],
  //     storage: sessionStorage,
  //     key: 'userSession'
  //   },
  //   {
  //     pick: ['userInfo'],
  //     storage: sessionStorage,
  //     key: 'userInfo'
  //   }
  // ]
})
