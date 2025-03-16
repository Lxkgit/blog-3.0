<!-- eslint-disable @typescript-eslint/no-explicit-any -->
<template>
  <transition
    enter-active-class="animate__animated animate__fadeInDown"
    leave-active-class="animate__animated animate__fadeOutUp"
    mode="in-out"
  >
    <header class="navigation-show" >
      <span v-show="props.kind === 'front'" class="left">
        <el-image
          style="width: 40px; height: 40px"
          :src="siteConfig.logo"
          :fit="'fill'"
        ></el-image>
        <span>{{ siteConfig.name }}</span>
      </span>
      <span class="middle">
        <el-menu :default-active="menuIndex" mode="horizontal">
          <el-menu-item index="1" @click="router.push('/')">
            <MyIcon type="icon-home3" />
            <span class="menu-title">首页</span>
          </el-menu-item>

          <el-menu-item index="2" @click="router.push('/category')">
            <MyIcon type="icon-article" />
            <span class="menu-title">文章</span>
          </el-menu-item>

          <el-menu-item index="3" @click="router.push('/document')">
            <MyIcon type="icon-book" />
            <span class="menu-title">文档</span>
          </el-menu-item>
          <el-menu-item index="4" @click="router.push('/classify')">
            <MyIcon type="icon-shijianzhou" />
            <span class="menu-title">归档</span>
          </el-menu-item>
        </el-menu>
      </span>
      <span class="right">
        <el-tooltip class="item" effect="dark" content="设置" placement="bottom">
          <span class="setting hvr-grow" @click="drawer = true" style="cursor: pointer">
            <MyIcon type="icon-setting" />
          </span>
        </el-tooltip>
        <el-tooltip class="item" effect="dark" content="搜索" placement="bottom">
          <span
            class="search hvr-grow"
            :style="{ color: menuIndex === '7' ? 'var(--el-color-primary)' : '' }"
            @click="router.push('/search')"
            style="cursor: pointer"
          >
            <MyIcon type="icon-search" />
          </span>
        </el-tooltip>
        <span class="user">
          <el-dropdown v-if="isLogin" @visible-change="dropdownChange">
            <span style="outline: 0; cursor: pointer">
              <el-avatar :src="photo"></el-avatar>
              <p>
                {{ userName }}
                <el-icon v-if="isDropdown">
                  <ArrowUp />
                </el-icon>
                <el-icon v-else>
                  <ArrowDown />
                </el-icon>
              </p>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="selfPage">个人中心</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <div v-else class="toLoginRegister">
            <span @click="toLogin">登录</span>
          </div>
        </span>
      </span>
      <el-drawer
        title="系统设置"
        v-model="drawer"
        :direction="'rtl'"
        :size="'25%'"
        :before-close="handleClose"
        destroy-on-close
      >
        <span>
          <el-divider></el-divider>
          <div class="display">
            <h4>显示模式</h4>
            <span>
              <img
                :class="isDark === true ? '' : 'img-active'"
                src="~@/assets/images/light.png"
                alt=""
              />
              <img
                :class="isDark === false ? '' : 'img-active'"
                src="~@/assets/images/dark.png"
                alt=""
              />
            </span>
            <el-switch
              style="display: block"
              v-model="isDarkSwitch"
              active-color="#303133"
              inactive-color="#f5f7fa"
              active-text="深色模式"
              inactive-text="浅色模式"
              @change="setDarkMode"
            />
          </div>
          <el-divider></el-divider>
          <div class="color">
            <h4>主题色</h4>
            <div>
              <el-tooltip
                v-for="(item, index) in themeList"
                :key="index"
                effect="dark"
                :content="item.name"
                placement="top"
              >
                <span
                  :style="{ backgroundColor: item.value }"
                  :class="colorValue === item.value ? 'color-active' : ''"
                  @click="colorChoose(item.value)"
                ></span>
              </el-tooltip>
            </div>
          </div>
          <el-divider></el-divider>
          <div v-if="props.kind === 'front'" class="nav-style">
            <h4>导航菜单</h4>
            菜单显示模式：
            <el-select v-model="navValue" @change="navChange">
              <el-option
                v-for="item in navigationList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
              </el-option>
            </el-select>
          </div>
          <div v-else>
            <h4>侧边菜单</h4>
            是否折叠菜单：
            <el-switch v-model="asideMenuFold" @change="asideMenuFoldChange" />
          </div>
          <el-divider></el-divider>
        </span>
      </el-drawer>
    </header>
  </transition>
  <div class="placeholder"></div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, onActivated } from "vue";
import icon from "@/utils/icon";
// import { ArrowDown, ArrowUp } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
// import user from "@/utils/user";
import { systemStore } from "@/store/system";
// import { tagsStore } from "@/store/tag";
import dark from "@/utils/dark";
import color from "@/utils/color";
import theme from "@/utils/theme";
import navigation from "@/utils/navigation";
import Cookies from 'js-cookie';

const store = systemStore();
// const tagStore = tagsStore();
const { isDark, setDark } = dark();
const { setTheme } = theme();
let { navigationList, setNavigation, navigationType } = navigation();
const router = useRouter();

let { MyIcon } = icon();
// // 引入用户信息模块
// let { isLogin, userName, logout } = user();
const { themeList } = color();
const props = defineProps({
  // 导航栏类型(前台后台)
  kind: {
    type: String,
    required: false,
    default: "front",
  },
});
//导航菜单-logo和name
const siteConfig = reactive({
  logo: "https://img2.baidu.com/it/u=2241198009,1203637343&fm=253&fmt=auto",
  name: "GSZero",
});

const selfPage = () => {
  // tagStore.activeTag("/admin/index");
  // router.push("/admin/index");
};

// 跳转至登录页
const toLogin = () => {
  //获取cookie的值
  let rzId = Cookies.get('rzId');
  let target = 'http://auth-server:60001/auth/oauth2/authorize?response_type=code&client_id=dianshang&scope=openid&redirect_uri=http://localhost:5173/callback'
  if (rzId) {
    //如果认证id不为空 带着认证id
    target = target + '&rzId=' + rzId;
  }
  //先登陆 在跳转到回调界面 获取授权码
  window.location.href = target
  // router.push({ path: "/loginRegister", query: { component: "Login" } });
};

// 个人中心-是否下拉状态
const isDropdown = ref(false);

// 个人中心-下拉事件
const dropdownChange = (value: any) => {
  isDropdown.value = value;
};

// 个人中心-用户头像
const photo = ref();

// 个人中心-获取用户头像
async function getPhotoData() {
  // let data = await getUserinfoId(userId.value)
  // photo.value = data.photo
}

//设置-菜单默认关闭
let drawer = ref(false);
//设置-菜单关闭事件
const handleClose = () => {
  drawer.value = false;
};
// 设置-显示模式默认值

const isDarkSwitch = ref(false);
// // 设置-切换是否设置暗黑模式
const setDarkMode = () => {
  setDark(isDarkSwitch.value);
};
// 设置-侧边菜单显示是否折叠
const asideMenuFold = ref(false);
// 设置-侧边菜单显示折叠切换事件
const asideMenuFoldChange = () => {
  store.setAsideMenuFold(asideMenuFold.value);
};

// 设置-默认主题色
const colorValue = ref("");
// 设置-切换主题色事件
const colorChoose = (value: any) => {
  colorValue.value = value;
  setTheme(colorValue.value);
};
// 设置-默认导航菜单样式
const navValue = ref("");

// 设置-导航菜单样式切换事件
const navChange = (value: any) => {
  setNavigation(value);
};

onActivated(() => {
  asideMenuFold.value = store.asideMenuFold;
});

onMounted(() => {
  asideMenuFold.value = store.asideMenuFold;
  // if (isLogin.value === true) {
  //   getPhotoData();
  // }
  colorValue.value = store.theme;
  navValue.value = store.navigation;
  isDarkSwitch.value = store.isDark;
});
// 当前激活的菜单id
const menuIndex = computed(() => store.menuIndex);
</script>

<style scoped>
header {
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--el-bg-color-overlay);
}

header .left {
  padding-left: 20px;
  width: 20%;
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--el-border-color);
  height: 60px;
}

header .left span {
  margin-left: 20px;
}

header .middle {
  flex: 1;
}

header .middle .menu-title {
  margin-left: 4px;
}

header .right {
  padding-right: 20px;
  width: 23%;
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--el-border-color);
  height: 58px;
  flex-direction: row-reverse;
}

header .right .user {
  display: flex;
  align-items: center;
}

header .right .user .toLoginRegister {
  cursor: pointer;
}

header .right .user .toLoginRegister span {
  margin: 0 10px;
}

header .right .user span {
  color: var(--el-text-color-regular);
  font-size: 14px;
}

header .right .user span p {
  display: inline;
  vertical-align: 14px;
  margin-left: 6px;
}

header .right .search,
header .right .setting {
  font-size: 25px;
  color: var(--el-text-color-regular);
  margin-left: 35px;
}

header h4 {
  font-weight: normal;
  color: var(--el-text-color-primary);
  margin-top: 40px;
}

header .display {
  color: var(--el-text-color-primary);
}

header .display img {
  width: 75px;
  height: 75px;
  margin: 0 20px 10px 20px;
  box-shadow: 0 2px 12px 0 gray;
  border-radius: 6px;
}

header .display .img-active {
  box-shadow: 0 2px 12px 0 #409eff;
}

header .color {
  color: var(--el-text-color-primary);
}

header .color span {
  display: inline-block;
  width: 30px;
  height: 30px;
  margin: 0 10px;
  border-radius: 5px;
  transition: all 0.5s;
  box-shadow: none;
  background-image: none;
}

header .color span:hover {
  text-decoration: underline;
  cursor: pointer;
}

header .color .color-active {
  box-shadow: 0 2px 13px 2px grey;
  background-image: url("/src/assets/images/yes.png");
  background-repeat: no-repeat;
}

header .nav-style {
  color: var(--el-text-color-primary);
}

header .nav-style .el-select {
  width: 120px;
}

.placeholder {
  height: 61px;
}

.navigation-show {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 5;
}

</style>
