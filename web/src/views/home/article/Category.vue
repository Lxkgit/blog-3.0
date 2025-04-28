<template>
  <section class="category">
    <NavMenu></NavMenu>
    <div class="page">
      <article class="animate__animated animate__fadeInLeft">
        <div class="article_type">
          <ArticleType @selectArticleFun="selectArticleFun"></ArticleType>
        </div>
        <div class="article-list">
          <el-card class="box-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">📜 文章列表</span>
              </div>
            </template>
            <ul>
              <li v-for="item in article.list" :key="item.id">
                <ArticleItem :article="item"></ArticleItem>
              </li>
            </ul>
            <div class="paging">
              <el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[5, 10, 15, 20]"
                layout="total, sizes, prev, pager, next, jumper" @current-change="pageChange" @size-change="sizeChange"
                :total="total">
              </el-pagination>
            </div>
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

<script setup name="Category" lang="ts">
import NavMenu from "@/components/home/NavMenu.vue";
import ArticleItem from "@/components/home/ArticleItem.vue";
import Aside from "@/components/home/Aside.vue";
import Footer from "@/components/home/Footer.vue";
import BackTop from "@/components/home/BackTop.vue";
import ArticleType from "@/components/home/article/ArticleType.vue";
import { onActivated, onMounted, reactive, ref } from "vue";
import { onBeforeRouteUpdate, useRouter } from "vue-router";
import { systemStore } from "@/store/system.ts";
import { getArticleListApi, getArticleTypeByIdApi } from "@/api/content";

const store = systemStore();
const router = useRouter();

// 文章分页信息
let page = ref<number>(1);
let size = ref<number>(5);
let total = ref<number>(0);

let articleTypeId = ref<number>(0);

// 文章分类名
let articleType: any = reactive({ date: [] });

// 获取文章分类名称
const articleTypeData = (articleTypeId: any) => {
  articleType.date = [];
  getArticleTypeByIdApi(articleTypeId).then((res: any) => {
    if (res.code === 200) {
      articleType.date = res.result;
    }
  });
};

// 根据文章分类查询文章
const selectArticleFun = (type: any) => {
  articleTypeId.value = type;
  page.value = 1;
  if (type !== 0) {
    articleTypeData(type);
  }
  articleData(page.value, size.value, articleTypeId.value);
};

// 文章列表
let article: any = reactive({ list: [] });

// 获取文章数据
const articleData = (page: any, size: any, articleTypeId: any) => {
  article.list = [];
  article.total = 0;
  const params = {
    pageNum: page,
    pageSize: size,
    type: 0,
    selectUser: 0,
    selectStatus: "1,2",
    sortType: "0,1",
    articleType: articleTypeId === 0 ? null : articleTypeId,
  };
  getArticleListApi(params).then((res: any) => {
    if (res.code === 200) {
      article.list = res.result.list;
      total = res.result.total;
    }
  });
};

// 分页-页面跳转
const pageChange = (page: any) => {
  window.scrollTo({ top: 0 })
  articleData(page, size.value, articleTypeId.value);
};

const sizeChange = (size: any) => {
  window.scrollTo({ top: 0 })
  articleData(1, size, articleTypeId.value);
}

onMounted(() => {
  articleData(1, size.value, articleTypeId.value);
});

onActivated(() => {
  store.menuIndex = "2"
});
</script>

<style scoped>
.category .article_type {
  margin-top: 15px;
}

.category .article-list {
  margin-top: 15px;
}

.category .article-list ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}
</style>
