<template>
  <section class="home">
    <NavMenu></NavMenu>
    <div class="page">
      <article class="animate__animated animate__fadeInLeft">
        <div class="carousel">
          <el-skeleton :loading="carouselLoading" animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 900px; height: 500px" />
            </template>
            <template #default>
              <el-carousel width="900px" height="500px" :interval="5000">
                <el-carousel-item v-for="carousel in carouselList" :key="carousel.id">
                  <el-image
                    class="pointer"
                    style="width: 900px; height: 500px"
                    :src="carousel.img"
                    :fit="'fill'"
                    :key="carousel.id"
                    @click="toCarousel(carousel.url)"
                  >
                    <template #placeholder>
                      <Loading type="image"></Loading>
                    </template>
                  </el-image>
                </el-carousel-item>
              </el-carousel>
            </template>
          </el-skeleton>
        </div>
        <div class="new">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">🆕 最新文章</span>
              </div>
            </template>
            <ul>
              <li v-for="item in article.list" :key="item.id">
                <ArticleItem :article="item"></ArticleItem>
              </li>
            </ul>
            <p
              class="isLoading"
              v-if="loading"
              v-loading="loading"
              element-loading-text="玩命加载中"
              element-loading-background="#ffffff"
            ></p>
            <p v-if="!noMore && article.count">
              <el-divider>我是有底线的</el-divider>
            </p>
          </el-card>
        </div>
      </article>
      <aside>
        <Aside></Aside>
      </aside>
    </div>
    <Footer></Footer>
    <BackTop></BackTop>
  </section>
</template>

<script setup name="Home" lang="ts">
import NavMenu from '@/components/home/NavMenu.vue'
import Footer from '@/components/home/Footer.vue'
import BackTop from '@/components/home/BackTop.vue'
import Loading from '@/components/common/Loading.vue'
import ArticleItem from '@/components/home/ArticleItem.vue'
import Aside from '@/components/home/Aside.vue'

import { computed, onActivated, onMounted, onUnmounted, reactive, ref } from 'vue'
import { getArticleListApi } from '@/api/content'
import { systemStore } from '@/store/system'

const store = systemStore()
//轮播图
const carouselList: any = ref([])

async function CarouselData() {
  carouselList.value = [
    {
      id: '1',
      url: 'https://www.baidu.com',
      img: 'https://img2.baidu.com/it/u=2241198009,1203637343&fm=253&fmt=auto',
    },
  ]
}

// 点击轮播图跳转
const toCarousel = (url: string) => {
  window.open(url)
}
// 轮播图加载动画是否开启
const carouselLoading = ref(true)
//最新文章列表
const article: any = reactive({
  list: [],
  count: 0,
})
// 是否还有更多需要加载
const noMore = computed(() => article.list.length < article.count)
// 文章请求参数
const article_params = {
  pageNum: 1,
  pageSize: 5,
  type: 0,
  selectUser: 0,
  selectStatus: '1,2',
  sortType: '0,1',
}
// 是否可以执行加载中动画
const loading = ref(false)

/**
 * 加载下一页
 */
const load = () => {
  getArticleListApi(article_params).then((res: any) => {
    if (res.code === 200) {
      article.list.push(...res.result.list)
      article.count = res.result.total
      loading.value = false
      article_params.pageNum = article_params.pageNum + 1
    }
  })
}
// 页面滚动事件
const scrollHandle = () => {
  const scrollHeight = document.body.scrollHeight || document.documentElement.scrollHeight
  const scrollTop = document.body.scrollTop || document.documentElement.scrollTop
  const clientHeight = document.documentElement.clientHeight
  const distance = scrollHeight - scrollTop - clientHeight
  if (distance <= 400 && noMore.value) {
    if (!loading.value) {
      loading.value = true
      setTimeout(() => {
        load()
      }, 300)
    }
  }
}
onMounted(() => {
  CarouselData()
  load()
  // 监听滚动事件
  window.addEventListener('scroll', scrollHandle, false)
  setTimeout(() => {
    carouselLoading.value = false
  }, 2000)
})
onUnmounted(() => {
  // 组件卸载时，停止监听
  window.removeEventListener('scroll', scrollHandle, false)
})
onActivated(() => {
  store.setMenuIndex('1')
})
</script>

<style scoped>
article .carousel {
  margin-bottom: 15px;
  background-color: var(--el-bg-color-overlay);
}

article .new ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

article .isLoading {
  padding: 30px;
  font-size: 30px;
}
</style>
