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
import NavMenu from "@/components/home/NavMenu.vue"
import Footer from "@/components/home/Footer.vue";
import BackTop from "@/components/home/BackTop.vue";
import Loading from "@/components/home/Loading.vue";
import ArticleItem from "@/components/home/ArticleItem.vue";
import Aside from "@/components/home/Aside.vue";


import { computed, onActivated, onMounted, onUnmounted, reactive, ref } from "vue";
// import { getArticleListApi } from "@/api/content";
import { systemStore } from "@/store/system";

const store = systemStore();
//轮播图
const carouselList: any = ref([]);

async function CarouselData() {
  carouselList.value = [
    {
      id: "1",
      url: "https://www.baidu.com",
      img: "https://img2.baidu.com/it/u=2241198009,1203637343&fm=253&fmt=auto",
    },
  ];
}

// 点击轮播图跳转
const toCarousel = (url: any) => {
  window.open(url);
};
// 轮播图加载动画是否开启
const carouselLoading = ref(true);
//最新文章列表
const article: any = reactive({
  list: [{
                "id": 117,
                "userId": 1,
                "title": "临时笔记",
                "contentMd": "## 博客3.0\n### 组件选择及对应版本\n\n|组件名称|功能说明|版本|对比2.0|备注|\n|-|-|-|-|-|\n|jdk |Java开发环境 |1.8|未变化|-|\n|nacos | 注册中心|v2.4.3|v2.1.0升级|-|\n|mysql|数据库|8.0.20|未变化|-|\n|redis|缓存数据库|6.2.17|6.2.5升级|-|\n|fauria/vsftpd|ftp|latest|未变化|-|\n|nginx|代理服务|1.20.2|未变化|-|\n|apache/rocketmq|消息队列|5.1.4|5.1.3升级|-|\n|elasticsearch|搜索和分析引擎|7.14.0|新引入|-|\n|minio|对象存储|latest|新引入|-|\n\n### maven 依赖变更选择\n\n|依赖名称|功能说明|版本|对比2.0|备注|\n|-|-|-|-|-|\n|spring-boot-dependencies|spring boot 依赖版本合集|2.7.18|2.2.6.RELEASE升级|-|\n|spring-cloud-dependencies|spring cloud 依赖版本合集|2021.0.8|Hoxton.SR3升级|-|\n|||||-|\n|||||-|\n|||||-|\n|||||-|\n|||||-|\n|||||-|\n|||||-|\n\n\n## 工具\nhttps://tendcode.com/tool/docker-search/\nhttps://mvnrepository.com/\nhttps://developer.aliyun.com/mvn/search\n\n\n\n\n\n# 使用Nginx变量和外部认证脚本\n如果你希望更灵活地处理认证（例如，通过外部服务或数据库），可以使用Nginx的变量和外部脚本。\n\n## 1. 编写认证脚本：\n\n你可以编写一个简单的Python或Shell脚本来验证用户名和密码。例如，使用Python：\n\n```python\n#!/usr/bin/env python3\nimport sys\nfrom passlib.context import CryptContext\n \n# 假设这是你的密码哈希\npwd_context = CryptContext(schemes=[\"bcrypt\"], deprecated=\"auto\")\ncorrect_password_hash = pwd_context.encrypt(\"yourpassword\")  # 使用bcrypt加密密码\n \ndef check_auth(username, password):\n    if username == \"yourusername\" and pwd_context.verify(password, correct_password_hash):\n        return True\n    return False\n \nif __name__ == \"__main__\":\n    username = sys.argv[1]\n    password = sys.argv[2]\n    if check_auth(username, password):\n        print(\"Authenticated\")\n        sys.exit(0)  # 成功认证返回0\n    else:\n        print(\"Authentication failed\")\n        sys.exit(1)  # 认证失败返回1\n```\n\n## 2. 配置Nginx以使用脚本：\n\n在Nginx配置中，使用ngx_http_auth_request_module模块调用此脚本：\n```\nlocation / {\n    set $auth_url /auth; # 定义认证URL路径\n    proxy_pass http://localhost; # 代理原始请求到实际服务地址\n    auth_request /auth; # 请求认证URL\n}\n \nlocation = /auth { # 定义认证处理逻辑的位置块\n    internal; # 标记为内部请求，不对外公开此地址\n    proxy_pass http://127.0.0.1:8000/check; # 代理到你的认证脚本地址和路径（例如Python Flask应用）\n    proxy_pass_request_body off; # 不传递请求体到认证脚本，因为认证通常不需要请求体信息。如果需要，可以调整此设置。\n    proxy_set_header Content-Length \"\"; # 如果不需要传递请求体，可以这样设置Content-Length头部为空。\n    proxy_set_header X-Original-URI $request_uri; # 传递原始URI到认证脚本。\n}\n```\n## 3. 运行认证脚本：\n\n确保你的认证脚本（例如上面的Python脚本）可以运行，并且Nginx可以访问到它。例如，你可以通过Flask运行它：\n\n```shell\nflask run --host=127.0.0.1 --port=8000\n```\n\n## 4. 重新加载Nginx：\n确保Nginx配置正确无误后，重新加载Nginx以应用更改。\n\n## 网站笔记\n- https://segmentfault.com/a/1190000045019749 \n- https://www.cnblogs.com/cgy1995/p/17533006.html\n- https://blog.csdn.net/baimao__Ch/article/details/142229097\n- https://blog.csdn.net/qq_38046739/article/details/127752149\n- https://blog.csdn.net/code_fly_/article/details/141396502?spm=1001.2014.3001.5502\n- https://blog.csdn.net/zzz6583zz/article/details/142101412\n- https://github.com/jobmission/oauth2-server\n- https://github.com/chensoul/spring-security-6-oauth2-samples\n",
                "contentImg": "http://49.232.129.253/files/1/article/img/2025-01-15_15-25-20_9083be_th.png",
                "contentMemo": "临时笔记",
                "articleType": "13",
                "articleLabel": "",
                "articleStatus": 1,
                "browseCount": 0,
                "likeCount": 0,
                "createTime": "2025-01-15 15:25:23",
                "updateTime": "2025-02-21 18:28:36",
                "articleIds": null,
                "pageSize": null,
                "pageNum": null,
                "type": null,
                "selectUser": null,
                "selectStatus": null,
                "sortType": null,
                "blogUser": {
                    "id": 1,
                    "username": "gszero",
                    "password": null,
                    "nickname": "GSZero",
                    "headImg": "https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg",
                    "email": "470687917@qq.com",
                    "status": 1,
                    "createTime": "2022-06-07 00:00:00",
                    "updateTime": "2022-06-07 02:02:03"
                },
                "articleTypes": [
                    {
                        "id": 13,
                        "parentId": 0,
                        "typeName": "JAVA",
                        "num": 4,
                        "node": 1,
                        "createUser": 1,
                        "createTime": "2023-04-17 23:15:20",
                        "updateTime": "2023-04-17 23:15:20"
                    }
                ],
                "articleLabels": null
            },
            {
                "id": 116,
                "userId": 1,
                "title": "【年终总结】2024年年终总结",
                "contentMd": "因为服务器上面操作失误，再加上为了省流量这几天没开树莓派定时同步数据，所以只能现在补写24年年终总结\n## 回首过去，2024年总结\n24年也攒到了40w，年初说好的要装一台台式电脑，但是因为今年工作变化有点大，一直也没机会，现在这个工作是长期出差的性质所以之后也没机会装机。先流水账的记录一下24年的经历吧，年初那会估计就要从大华离职了，所以最初那会上班就有点摆烂，每天我到点就溜，果然四月初就通知了我离职，可以选n和n+1，区别就是选n可以多上一个月班，并且没有任务，可以在上班时间准备找工作，提供一个月的工位也交一个月的社保公积金等，但是因为钱没差多少，而且选了n还要在继续上一个月班，当时实在是坚持不下去了拿钱就跑路，之后就把全部行李都打包寄回了家里面，一个自行车还有衣服、被子、热水器的杂物，总共打包了3个大包裹，邮回去花了五百多，主要还是自行车的运费有点贵，四月到七月一直在家里躺平，总共休息了三个半月时间吧，不得不说躺家里还是挺爽的，可惜没收入会焦虑，七月底找到了三份工作。一个是外包就完全没有考虑，还有一个是视频行业的一个公司，当时因为觉得这个公积金交的有点低就没去，最后去了福建正孚软件有限公司，说好的12%公积金，我去了才知道试用期不交，真的是服了，初入社会真是要踩不少坑啊，去之前说的加班到九月左右，我想这也就一个月时间回家吧，而且这个可以住公司的宿舍，各种权衡之下感觉这个工作还是不错的。最后去了实际情况是每天都要十点上班、晚上十点下班，加上北京的通勤时间加俩小时，几乎一整天都在围着工作转，甚至还有些卷王晚上十点之后还要继续工作，双休从来没享受过，还有好几次去了军委晚上一直忙到俩三点，如果第二天是工作日还要正常上班，周末那这个周末就直接废了，国庆七天一天都没休息，最后真是实在受不了了，我想着离职，项目领导看我不能加班也想着劝退我，所以说的国庆七天有三倍工资，而且这几个月公司也包住宿了，就好聚好散吧，我主动提了离职，然后十一月初离职回家又躺了一个月，这次找工作很快，还没到家就有了一个offer，好像是叫南京华苏科技，因为是外包，而且我刚离职想回家休息几天，就拒绝了这个offer，之后十二月初找到了现在的这家公司，杭州康旭科技有限公司（md我有罪啊，公司名字给忘了，翻邮件才知道公司叫啥名字），到目前入职一个月了出差没租房子，工作也从来不加班，目前感觉还是很不错的一个公司，昨天还给发了600的过年购物券，这个几乎算是现金了可以96折卖出去，但是我还是留着自己用了，今年的流水账差不多就写这些吧，然后就是博客的开发，一整年把博客服务的功能差不多完善了，树莓派的数据同步功能也做好了，可以前几天一直省流量没开导致丢了半个月的数据，这下长记性了，记得去年定好的要在24年把单片机的数据上报功能弄好，这个数据上保是有的，但是实在是太过简略了，打算25年把单片机数据上报的各个模块要统一设计，功能都模块化，这部分内容等会写在25年计划中。差不多就写这些吧，博客更新主要还是涉及到服务部署的这部分，整体都改成了docker一键部署，但是docker下载经常会下载失败，也不知道是什么情况，之后准备把docker改成离线安装包部署，用到的其他镜像文件也都下载下来，接下来就看看去年定好的目标吧，一年一度的看乐子时间，可惜今年第二次了：\n\n1. web主要功能现在已经确定了，做好基础的维护和新增一些简单的小功能\n   - **新增了数据同步，一键部署吧，没其他功能了**\n2. 继续记录笔记、文档、日记等功能\n   - **笔记、文档、日记这下都一直记录着，日记是一天都没落下的记录完成了，文档主要是记录了面试准备的文档**\n3. 把硬件环境搭建好，这个必须完成（要是10月还没弄好就集中全力做这个）\n   - **硬件环境差不多也好了，主要是用树莓派进行中转，其他单片机都连接在树莓派的mqtt下，整体流程是没问题的，因为博客硬件数据上报这块流程在十月份那会也整体进行了一次大修改，主要是用来兼容各种模块命令下发，用到了反射整体流程还是可以的**\n4. 12次骑行以及骑行记录\n   - **骑行十二次看肯定是有的，但是因为今年变化有点大，就没有记录** \n5. 初步开发app和客户端\n   - **这个鸽了，app准备使用Java开发，客户端准备使用pyqt6开发，有时间继续学习** \n6. 白金俩个游戏\n   - **白金和黑魂一和土豆兄弟，总的感觉黑魂一还是很不错的，对魂一的地图印象很深刻，到玩魂二的时候可能是因为地图太大，或者魂二一开始就可以全图篝火传送就经常记不住地图，有时候找个boss都找不到**\n7. 2025年的时候回顾现在，去年计划我觉得实现的还行，希望今年也继续努力\n   - **总体评价还可以吧**\n\n## 展望未来，2025年计划\n25年工作方面就在这家公司一直干着，预计俩三年吧，之后如过经济环境变好可以早点离职去换下一份工作，看去年还想着找一个15-16k的工作，现在实际情况是13的都不好找，而且北京很多都是13k，我这个在杭州13k在出差到台州算是高薪工作了。博客开发要侧重单片机方向的开发，要多做几个模块，先展望一下，定一个目标，不然怎么有动力去实现计划呢，首先第一个是电脑数据监测模块，要接入舵机用于控制电脑开关、温湿度传感器用于监测电脑温度湿度、噪音传感器用于监测电脑风扇声音大小、有能力可以再弄一个摄像头模块再加俩个舵机充当云台，要加摄像头肯定要弄流媒体服务器，这块又是新的一块业务功能，可以推迟开发，第二个模块要弄家庭空气监测主要就是监测各种气体吧，都是些传感器，这块应该不是很难，第三个要加语音识别模块，通过stm32语音识别然后来控制其他模块服务或者其它博客相关功能，只要语言识别了那就有无限可能，第四个就是充电相关的模块，首先是太阳能充电模块，要可以使用太阳能版对本模块的电池进行充电，太阳能板要加光源追踪功能，然后连接的板子要有可以输出不同接口，不同电压的接口，用于未来连接树莓派和数据备份的机械硬盘，还有就是要可以对其它模块使用的电池模块进行充电，无线充电和有线充电都加上吧，理想总得丰满点，最后一个模块就是通用的电池模块，要可以和对应的模块进行通信确定哪个模块用哪块电池，数据全都要手机上报，电池模块要有电量数据上报。单片机之外就是pyqt6和app可以先不急，慢慢看教程学习留好笔记就行了。除了技术上这些开发，今年想要出去流浪一次，带着我的睡袋，再买个帐篷，随便找个城市出去当一次流浪汉，直接写出总结吧：\n1. 硬件模块开发，要画板子、3d打印模型做一个完美的模块\n2. 文档、笔记、日记都要记录\n3. 白金俩个游戏\n4. 当一次流浪汉，去一个陌生城市\n5. 初步搭建app和电脑客户端的模块\n6. 重构博客，各个组件模块化，优雅实现代码功能\n7. 2026年的时候回顾现在\n\n## 其他事情\n\n### GitHub开发记录\n![image.png](http://49.232.129.253/files/1/article/img/2025-01-14_23-39-48_75017e_image.png)\n四月到七月主要是离职了焦虑，在家天天躺平，也总结写一些文档记录，八月和十一月是当时的那家公司实在是太累了，九月是中午休息时间抽时间开发，完成和博客设备功能的重新开发\n\n## b站记录\n![image.png](http://49.232.129.253/files/1/article/img/2025-01-14_23-45-52_7a5407_image.png)\n我在b站上班，md都全勤了\n\n### 游戏\n![image.png](http://49.232.129.253/files/1/article/img/2025-01-14_23-48-44_346859_image.png)\n\nswitch已经是吃灰了，epic还是从来不开，steam玩了时间还挺长的，450小时了\n\n### 读书\n？\n为什么没有？因为没读，好几本书都是看了一半看不下去了，浮躁\n\n## 结尾\n心累啊，一时失误数据都丢了，1月5日完成的第一次总结，1月14日总结补写完成\n2025年1月14日 23点52分 年终总结补写完成\n\n\n\n\n\n\n\n\n",
                "contentImg": "http://49.232.129.253/files/1/article/img/2025-01-14_21-02-30_ce93ba_DWS88eyWIdZM1xethumb1000_0.png",
                "contentMemo": "2024年年终总结（误删补写）",
                "articleType": "18",
                "articleLabel": "6",
                "articleStatus": 1,
                "browseCount": 0,
                "likeCount": 0,
                "createTime": "2025-01-14 21:02:33",
                "updateTime": "2025-01-14 23:52:20",
                "articleIds": null,
                "pageSize": null,
                "pageNum": null,
                "type": null,
                "selectUser": null,
                "selectStatus": null,
                "sortType": null,
                "blogUser": {
                    "id": 1,
                    "username": "gszero",
                    "password": null,
                    "nickname": "GSZero",
                    "headImg": "https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg",
                    "email": "470687917@qq.com",
                    "status": 1,
                    "createTime": "2022-06-07 00:00:00",
                    "updateTime": "2022-06-07 02:02:03"
                },
                "articleTypes": [
                    {
                        "id": 18,
                        "parentId": 0,
                        "typeName": "年终总结",
                        "num": 2,
                        "node": 0,
                        "createUser": 1,
                        "createTime": "2023-04-17 23:15:20",
                        "updateTime": "2023-04-17 23:15:20"
                    }
                ],
                "articleLabels": [
                    {
                        "id": 6,
                        "userId": 1,
                        "labelType": 7,
                        "labelName": "年终总结",
                        "articleNum": 4,
                        "createTime": "2024-01-01 11:27:07",
                        "updateTime": "2024-01-01 11:27:07"
                    }
                ]
            },
            {
                "id": 114,
                "userId": 1,
                "title": "Spring boot使用aop详解",
                "contentMd": "# Spring boot 使用 AOP\n\n## AOP 是什么\n\nAOP（Aspect Oriented Programming）意为：面向切面编程，通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术。AOP是OOP的延续，是软件开发中的一个热点，也是Spring框架中的一个重要内容，是函数式编程的一种衍生范型。\n\n利用AOP可对业务逻辑进行增强，在不改变原有逻辑的基础上，在其前后进行处理。降低了耦合性，减少了大量冗余的操作。特别适合用于大量方法都需要进行相同处理的操作。\n\n## AOP 概念\n\nAOP 切面就是在不破坏原有方法基础上对原有方法进行切面在其执行前后进行处理\n\n### 切面（Aspect）：一般是指被@Aspect修饰的类，代表着某一具体功能的AOP逻辑。\n### 切入点（Pointcut）：选择对哪些方法进行增强。\n### 通知（Advice）：对目标方法的增强，有一下五种增强的类型。\n- 环绕通知（@Around）：内部执行方法，可自定义在方法执行的前后操作。\n- 前置通知（@Before）：在方法执行前执行。\n- 后置通知（@After）：在方法执行后执行。\n- 返回通知（@AfterReturning）：在方法返回后执行。\n- 异常通知（@AfterThrowing）：在方法抛出异常后执行。\n\n### 连接点（JoinPoint）：就是那些被切入点选中的方法。这些方法会被增强处理。\n\n常用的方法是execution()和@annotation\n\n- execution(修饰符 返回值类型 方法名（参数）异常)\n\n![execution](https://img-blog.csdnimg.cn/6bd98324c2bc4119957fbb25b2eb1ce9.png)\n\n| 语法参数   | 描述                                                                                                                                                                                         |\n| ---------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |\n| 修饰符     | 可选，如public，protected，写在返回值前，任意修饰符填*号就可以                                                                                                                               |\n| 返回值类型 | 必选，可以使用*来代表任意返回值                                                                                                                                                              |\n| 方法名     | 必选，可以用*来代表任意方法                                                                                                                                                                  |\n| 参数       | ()代表是没有参数，(…)代表是匹配任意数量，任意类型的参数，当然也可以指定类型的参数进行匹配，如要接受一个String类型的参数，则(java.lang.String), 任意数量的String类型参数：(java.lang.String…) |\n| 异常       | 可选，语法：throws 异常，异常是完整带包名，可以是多个，用逗号分隔                                                                                                                            |\n\n > 使用示例\n```java\n// 所有方法\nexecution(* *..*(..))\n// 指定参数，即入参本身的类型，不能放其接口、父类\nexecution(* *..*(java.lang.String, java.lang.String)\n// 指定方法前缀\nexecution(* *..*.prefix*(..))\n// 指定方法后缀\nexecution(* *..*.*suffix(..))\n// 组合，增强所有方法，但是去掉指定前缀和指定后缀的方法\nexecution(* *..*(..)) && (!execution(* *..prefix*(..)) || !execution(* *..*suffix(..)))\n\n// 示例\n@Pointcut(\"execution(* com.dahua.evo.orms.business.controller..*.*(..))\")\npublic void doValidate() {\n}\n```\t\n\n- @annotation()\n\n> 使用示例\n```java\n// 增强被指定注解修饰的方法（所有加了@TestAspect注解的都会被）\n@annotation(com.banmoon.test.annotation.TestAspect)\n// 指定前缀的注解修饰的方法\n@annotation(com.banmoon.test.annotation.Prefix*)\n// 指定后缀的注解修饰的方法\n@annotation(com.banmoon.test.annotation.*Suffix)\n\n@Pointcut(\"@annotation(org.springframework.web.bind.annotation.RequestMapping)\")\npublic void webLogAspect() {\n}\n```\n\n## Spring boot中使用AOP\n\n### 1. 引入依赖\n```xml\n<!-- spring-boot-starter-web 中已经包含了aop  -->\n<dependency>\n  <groupId>org.springframework.boot</groupId>\n  <artifactId>spring-boot-starter-web</artifactId>\n</dependency>\n<dependency>\n    <groupId>org.springframework.boot</groupId>\n    <artifactId>spring-boot-starter-aop</artifactId>\n</dependency>\n\n```\n\n### 2. 定义注解\n```java\nimport java.lang.annotation.ElementType;\nimport java.lang.annotation.Retention;\nimport java.lang.annotation.RetentionPolicy;\nimport java.lang.annotation.Target;\n\n@Retention(RetentionPolicy.RUNTIME)\n@Target(ElementType.METHOD)\npublic @interface Auth {\n\n}\n```\n\n### 3. 创建一个切面类\n```java\nimport org.aspectj.lang.JoinPoint;\nimport org.aspectj.lang.ProceedingJoinPoint;\nimport org.aspectj.lang.annotation.*;\nimport org.aspectj.lang.reflect.MethodSignature;\nimport org.springframework.stereotype.Component;\nimport java.lang.reflect.Method;\n\n@Aspect\n@Component\npublic class AuthAspect {\n    /**\n     * 定义了一个切点\n     * 这里的路径填自定义注解的全路径\n     */\n    @Pointcut(\"@annotation(com.zz.business.annotations.Auth)\")\n    public void authCut() {\n\n    }\n\t\n    @Before(\"authCut()\")\n    public void cutProcess(JoinPoint joinPoint) {\n        MethodSignature signature = (MethodSignature) joinPoint.getSignature();\n        Method method = signature.getMethod();\n        System.out.println(\"注解方式AOP开始拦截, 当前拦截的方法名: \" + method.getName());\n    }\n\n    @After(\"authCut()\")\n    public void after(JoinPoint joinPoint) {\n        MethodSignature signature = (MethodSignature) joinPoint.getSignature();\n        Method method = signature.getMethod();\n        System.out.println(\"注解方式AOP执行的方法 :\" + method.getName() + \" 执行完了\");\n    }\n\n\n    @Around(\"authCut()\")\n    public Object testCutAround(ProceedingJoinPoint joinPoint) throws Throwable {\n        System.out.println(\"注解方式AOP拦截开始进入环绕通知.......\");\n        Object proceed = joinPoint.proceed();\n        System.out.println(\"准备退出环绕......\");\n        return proceed;\n    }\n\n    /**\n     * returning属性指定连接点方法返回的结果放置在result变量中\n     *\n     * @param joinPoint 连接点\n     * @param result    返回结果\n     */\n    @AfterReturning(value = \"authCut()\", returning = \"result\")\n    public void afterReturn(JoinPoint joinPoint, Object result) {\n        MethodSignature signature = (MethodSignature) joinPoint.getSignature();\n        Method method = signature.getMethod();\n        System.out.println(\"注解方式AOP拦截的方法执行成功, 进入返回通知拦截, 方法名为: \" + method.getName() + \", 返回结果为: \" + result.toString());\n    }\n\n    @AfterThrowing(value = \"authCut()\", throwing = \"e\")\n    public void afterThrow(JoinPoint joinPoint, Exception e) {\n        MethodSignature signature = (MethodSignature) joinPoint.getSignature();\n        Method method = signature.getMethod();\n        System.out.println(\"注解方式AOP进入方法异常拦截, 方法名为: \" + method.getName() + \", 异常信息为: \" + e.getMessage());\n    }\n}\n\n```\n\n### 4. 连接点方法\n```java\n// 该方法加了上面自定义的注解 @Auth\n@RestController\n@RequestMapping(\"/company\")\n@RefreshScope\npublic class CompanyController {\n\t\n    @Auth\n    @GetMapping(\"/aopTest\")\n    public AjaxResult aopTest(@RequestParam String name){\n        //远程调用\n        System.out.println(\"正在执行接口name\" + name);\n        return AjaxResult.success(\"执行成功\" + name);\n    }\n}\n\n```",
                "contentImg": "http://49.232.129.253/files/1/article/img/2024-04-08_20-43-44_133787_8.png",
                "contentMemo": "springboot aop的概念与使用方法（需要修改）",
                "articleType": "6,47",
                "articleLabel": "9,10",
                "articleStatus": 1,
                "browseCount": 0,
                "likeCount": 0,
                "createTime": "2024-04-08 20:41:09",
                "updateTime": "2024-04-08 20:44:18",
                "articleIds": null,
                "pageSize": null,
                "pageNum": null,
                "type": null,
                "selectUser": null,
                "selectStatus": null,
                "sortType": null,
                "blogUser": {
                    "id": 1,
                    "username": "gszero",
                    "password": null,
                    "nickname": "GSZero",
                    "headImg": "https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg",
                    "email": "470687917@qq.com",
                    "status": 1,
                    "createTime": "2022-06-07 00:00:00",
                    "updateTime": "2022-06-07 02:02:03"
                },
                "articleTypes": [
                    {
                        "id": 6,
                        "parentId": 0,
                        "typeName": "Spring",
                        "num": 1,
                        "node": 1,
                        "createUser": 1,
                        "createTime": "2023-04-17 23:15:20",
                        "updateTime": "2023-04-17 23:15:20"
                    },
                    {
                        "id": 47,
                        "parentId": 6,
                        "typeName": "AOP",
                        "num": 1,
                        "node": null,
                        "createUser": 1,
                        "createTime": "2024-04-08 20:40:30",
                        "updateTime": "2024-04-08 20:40:30"
                    }
                ],
                "articleLabels": [
                    {
                        "id": 9,
                        "userId": 1,
                        "labelType": 1,
                        "labelName": "spring boot",
                        "articleNum": 1,
                        "createTime": "2024-04-08 20:44:03",
                        "updateTime": "2024-04-08 20:44:03"
                    },
                    {
                        "id": 10,
                        "userId": 1,
                        "labelType": 1,
                        "labelName": "aop",
                        "articleNum": 1,
                        "createTime": "2024-04-08 20:44:10",
                        "updateTime": "2024-04-08 20:44:10"
                    }
                ]
            },
            {
                "id": 112,
                "userId": 1,
                "title": "Java线程池简介",
                "contentMd": "# new Thread() 弊端\n\n执行异步任务简单的的写法就是\n\n```java\nnew Thread(new Runnable() {\n \n    @Override\n    public void run() {\n        // TODO Auto-generated method stub\n    }\n\n}).start();\n```\n\n弊端:\n1. 每次使用异步操作都需要new Thread新建对象，性能差\n2. 缺乏统一管理，各个线程之间可能会相互竞争造成死锁或内存溢出\n3. 缺乏更多的功能，如定时执行、定期执行、线程中断\n\nnew Thread的弊端那就是线程池的优点\n1. 重用存在的线程，减少对象创建、消亡的开销，性能佳\n2. 可有效控制最大并发线程数，提高系统资源的使用率，同时避免过多资源竞争，避免堵塞\n3. 供定时执行、定期执行、单线程、并发数控制等功能\n\n# Java线程池\n\n线程池核心属性\n- corePoolSize，核心线程数量，线程池的线程数量会维持在这个数字上\n- maximumPoolSize，最大线程数量，创建的线程数量不会超过这个数字\n- keepAliveTime，线程存活时间，超过核心线程数的线程，如果空闲时间超过指定时间，就会被回收\n- 任务队列，用于接收、存储待执行的任务，当线程空闲下来时，会从任务队列中取出任务并执行\n  - 直接交接队列（SynchronousQueue），队列大小为零，新任务直接开始运行，不会等待\n  - 有界队列(ArrayBlockQueue)，任务队列是有限的\n  - 无界队列(LinkedBlockQueue)，任务队列是无限的，理论上可以添加任意数量的任务\n\nJava通过Executors提供四种线程池（线程池核心属性的二次封装）\n- `newCachedThreadPool` 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。\n- `newFixedThreadPool` 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待\n- `newScheduledThreadPool` 创建一个定长线程池，支持定时及周期性任务执行\n- `newSingleThreadExecutor` 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行\n\n## `newCachedThreadPool`\n\n```java\n/*\n * 1.newCachedThreadPool\n * 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程\n * 线程池为无限大，当执行第二个任务时第一个任务已经完成，会复用执行第一个任务的线程，而不用每次新建线程\n */\nExecutorService cachedThreadPool = Executors.newCachedThreadPool();\n\n// 使用线程池\ncachedThreadPool.execute(new Runnable() {\n    public void run() {\n        // todo\n    }\n});\n```\n\n## `newFixedThreadPool`\n\n```java\n/*\n * 2.newFixedThreadPool\n * 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待\n * 定长线程池的大小最好根据系统资源进行设置。如Runtime.getRuntime().availableProcessors()\n */\nExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);\n\n// 使用线程池\nfixedThreadPool.execute(new Runnable() {\n\tpublic void run() {\n\t\t// todo\n\t}\n});\n\n```\n\n## `newScheduledThreadPool`\n\n```java\n/*\n * 3.newScheduledThreadPool\n * 创建一个定长线程池，支持定时及周期性任务执行\n * ScheduledExecutorService比Timer更安全，功能更强大\n */\nScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);\n\n//表示延迟1秒后每3秒执行一次\nscheduledThreadPool.scheduleAtFixedRate(new Runnable() {\n\tpublic void run() {\n\t\t// todo\n\t}\n}, 1, 3, TimeUnit.SECONDS);\n```\n\n## `newSingleThreadExecutor`\n\n```java\n/*\n * 4.newSingleThreadExecutor\n * 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行\n */\nExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();\n\nsingleThreadExecutor.execute(new Runnable() {\n\tpublic void run() {\n        // todo\n\t}\n});\n\n```\n\n# ExecutorService的方法\n\n- execute(Runnable command): 提交一个Runnable任务给线程池去执行\n- submit(Callable<T> task): 提交一个Callable任务给线程池去执行，并返回一个Future对象以获取任务的结果\n- submit(Runnable task, T result): 提交一个Runnable任务给线程池去执行，并返回一个Future对象，该对象将持有一个由给定result提供的结果\n- shutdown(): 启动线程池的关闭序列。已提交的任务将执行完毕，但不会接受新的任务\n- shutdownNow(): 尝试停止所有正在执行的任务，暂停处理正在等待的任务，并返回等待执行的任务列表\n\n## Future的方法\n\n- get(): 等待任务完成，然后返回结果\n- get(long timeout, TimeUnit unit): 等待任务在给定的时间内完成，然后返回结果。如果超时，则抛出TimeoutException\n- cancel(boolean mayInterruptIfRunning): 尝试取消此任务的执行。如果任务已经完成、已被取消或由于某些其他原因而不能取消，则返回false\n- isDone(): 如果任务已经完成，则返回true\n- isCancelled(): 如果任务在完成前被取消，则返回true",
                "contentImg": "http://49.232.129.253/files/1/article/img/2024-03-31_16-56-23_c171ee_7.png",
                "contentMemo": "问什么要使用线程池以及常用的线程池用法",
                "articleType": "13,46",
                "articleLabel": "8",
                "articleStatus": 1,
                "browseCount": 0,
                "likeCount": 0,
                "createTime": "2024-03-31 16:51:20",
                "updateTime": "2024-03-31 16:56:25",
                "articleIds": null,
                "pageSize": null,
                "pageNum": null,
                "type": null,
                "selectUser": null,
                "selectStatus": null,
                "sortType": null,
                "blogUser": {
                    "id": 1,
                    "username": "gszero",
                    "password": null,
                    "nickname": "GSZero",
                    "headImg": "https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg",
                    "email": "470687917@qq.com",
                    "status": 1,
                    "createTime": "2022-06-07 00:00:00",
                    "updateTime": "2022-06-07 02:02:03"
                },
                "articleTypes": [
                    {
                        "id": 13,
                        "parentId": 0,
                        "typeName": "JAVA",
                        "num": 4,
                        "node": 1,
                        "createUser": 1,
                        "createTime": "2023-04-17 23:15:20",
                        "updateTime": "2023-04-17 23:15:20"
                    },
                    {
                        "id": 46,
                        "parentId": 13,
                        "typeName": "线程池",
                        "num": 2,
                        "node": null,
                        "createUser": 1,
                        "createTime": "2024-03-31 16:46:49",
                        "updateTime": "2024-03-31 16:46:49"
                    }
                ],
                "articleLabels": [
                    {
                        "id": 8,
                        "userId": 1,
                        "labelType": 1,
                        "labelName": "Java线程池",
                        "articleNum": 2,
                        "createTime": "2024-03-31 16:52:26",
                        "updateTime": "2024-03-31 16:52:26"
                    }
                ]
            },
            {
                "id": 111,
                "userId": 1,
                "title": "Java锁",
                "contentMd": "# 锁的分类\n\n![java锁.png](http://49.232.129.253/files/1/article/img/2024-03-31_16-47-52_ffd0c0_java锁.png)\n\n## 乐观锁和悲观锁\n悲观锁认为自己在使用数据的时候一定有别的线程来修改数据，因此在获取数据的时候会先加锁，确保数据不会被别的线程修改。Java中，synchronized关键字和Lock的实现类都是悲观锁（比如ReentrantLock）。\n\n乐观锁在Java中是通过使用无锁编程来实现，最常采用的是CAS算法，Java原子类中的递增操作就通过CAS自旋实现的。（比如 AtomicLong）\n\n### CAS\nCAS全称 Compare And Swap（比较与交换），是一种无锁算法。在不使用锁（没有线程被阻塞）的情况下实现多线程之间的变量同步。java.util.concurrent包中的原子类就是通过CAS来实现了乐观锁。\n\nCAS算法涉及到三个操作数：\n- 需要读写的内存值 V。\n- 进行比较的值 A。\n- 要写入的新值 B。\n\n当且仅当 V 的值等于 A 时，CAS通过原子方式用新值B来更新V的值（“比较+更新”整体是一个原子操作），否则不会执行任何操作。一般情况下，“更新”是一个不断重试的操作。\n\nCAS存在的问题：\n\n1. ABA问题\n- CAS需要在操作值的时候检查内存值是否发生变化，没有发生变化才会更新内存值。但是如果内存值原来是A，后来变成了B，然后又变成了A，那么CAS进行检查时会发现值没有发生变化，但是实际上是有变化的。ABA问题的解决思路就是在变量前面添加版本号，每次变量更新的时候都把版本号加一，这样变化过程就从“A－B－A”变成了“1A－2B－3A”。\n  - JDK从1.5开始提供了AtomicStampedReference类来解决ABA问题，具体操作封装在compareAndSet()中。compareAndSet()首先检查当前引用和当前标志与预期引用和预期标志是否相等，如果都相等，则以原子方式将引用值和标志的值设置为给定的更新值。\n\n2. 循环时间长开销大。CAS操作如果长时间不成功，会导致其一直自旋，给CPU带来非常大的开销。\n\n3. 只能保证一个共享变量的原子操作，对一个共享变量执行操作时，CAS能够保证原子操作，但是对多个共享变量操作时，CAS是无法保证操作的原子性的。\n   - Java从1.5开始JDK提供了AtomicReference类来保证引用对象之间的原子性，可以把多个变量放在一个对象里来进行CAS操作。\n\n## 自旋锁和适应性自旋锁\n\n阻塞或唤醒一个Java线程需要操作系统切换CPU状态来完成，这种状态转换需要耗费处理器时间。如果同步代码块中的内容过于简单，状态转换消耗的时间有可能比用户代码执行的时间还要长。\n\n在许多场景中，同步资源的锁定时间很短，为了这一小段时间去切换线程，线程挂起和恢复现场的花费可能会让系统得不偿失。如果物理机器有多个处理器，能够让两个或以上的线程同时并行执行，我们就可以让后面那个请求锁的线程不放弃CPU的执行时间，看看持有锁的线程是否很快就会释放锁。\n\n而为了让当前线程“稍等一下”，我们需让当前线程进行自旋，如果在自旋完成后前面锁定同步资源的线程已经释放了锁，那么当前线程就可以不必阻塞而是直接获取同步资源，从而避免切换线程的开销。这就是自旋锁。\n\n自旋锁本身是有缺点的，它不能代替阻塞。自旋等待虽然避免了线程切换的开销，但它要占用处理器时间。如果锁被占用的时间很短，自旋等待的效果就会非常好。反之，如果锁被占用的时间很长，那么自旋的线程只会白浪费处理器资源。所以，自旋等待的时间必须要有一定的限度，如果自旋超过了限定次数（默认是10次，可以使用-XX:PreBlockSpin来更改）没有成功获得锁，就应当挂起线程。\n\n自旋锁的实现原理同样也是CAS，AtomicInteger中调用unsafe进行自增操作的源码中的do-while循环就是一个自旋操作，如果修改数值失败则通过循环来执行自旋，直至修改成功。\n\n自旋锁在JDK1.4.2中引入，使用-XX:+UseSpinning来开启。JDK 6中变为默认开启，并且引入了自适应的自旋锁（适应性自旋锁）。\n\n自适应意味着自旋的时间（次数）不再固定，而是由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定。如果在同一个锁对象上，自旋等待刚刚成功获得过锁，并且持有锁的线程正在运行中，那么虚拟机就会认为这次自旋也是很有可能再次成功，进而它将允许自旋等待持续相对更长的时间。如果对于某个锁，自旋很少成功获得过，那在以后尝试获取这个锁时将可能省略掉自旋过程，直接阻塞线程，避免浪费处理器资源。\n\n## 公平锁和非公平锁\n公平锁是指多个线程按照申请锁的顺序来获取锁。非公平锁是指多个线程获取锁的顺序并不是按照申请锁的顺序，有可能后申请的线程比先申请的线程优先获取锁。有可能，会造成优先级反转或者饥饿现象。对于Java ReentrantLock而言，通过构造函数指定该锁是否是公平锁，默认是非公平锁。非公平锁的优点在于吞吐量比公平锁大。对于Synchronized而言，也是一种非公平锁。由于其并不像ReentrantLock是通过AQS的来实现线程调度，所以并没有任何办法使其变成公平锁。\n\n## 可重入锁和非可重入锁\n可重入锁又名递归锁，是指在同一个线程在外层方法获取锁的时候，再进入该线程的内层方法会自动获取锁（前提锁对象得是同一个对象或者class），不会因为之前已经获取过还没释放而阻塞。Java中ReentrantLock和synchronized都是可重入锁，可重入锁的一个优点是可一定程度避免死锁。\n\n## 独占锁和共享锁\n独享锁也叫排他锁，是指该锁一次只能被一个线程所持有。如果线程T对数据A加上排它锁后，则其他线程不能再对A加任何类型的锁。获得排它锁的线程即能读数据又能修改数据。共享锁是指该锁可被多个线程所持有。如果线程T对数据A加上共享锁后，则其他线程只能对A再加共享锁，不能加排它锁。获得共享锁的线程只能读数据，不能修改数据。\n\nJava中的ReentrantReadWriteLock就是读写锁；读锁是共享锁，同时可以多个线程获取；写锁的独占锁，同时只能一个线程获取。当有读锁时，写锁就不能获得；而当有写锁时，除了获得写锁的这个线程可以获得读锁外，其他线程不能获得读锁。\n\n## 无锁、偏向锁、轻量级锁和重量级锁\n这四种锁是指锁的状态，专门针对synchronized的\n\n### 无锁\n\n无锁没有对资源进行锁定，所有的线程都能访问并修改同一个资源，但同时只有一个线程能修改成功。\n\n### 偏向锁\n\n偏向锁是指一段同步代码一直被一个线程所访问，那么该线程会自动获取锁，降低获取锁的代价。\n\n### 轻量级锁\n\n是指当锁是偏向锁的时候，被另外的线程所访问，偏向锁就会升级为轻量级锁，其他线程会通过自旋的形式尝试获取锁，不会阻塞，从而提高性能。\n\n### 重量级锁\n\n升级为重量级锁时，对象头中锁标志的状态值变为“10”，此时Mark Word中存储的是指向重量级锁的指针，此时等待锁的线程都会进入阻塞状态。\n\n升级的大概流程：\n\n一开始处于无锁的状态，为了不让这个线程每次获得锁都需要CAS操作的性能消耗，就引入了偏向锁。当一个线程访问对象并获取锁时，会在对象头里存储锁偏向的这个线程的ID，以后该线程再访问该对象时只需判断对象头的Mark Word里是否有这个线程的ID，如果有就不需要进行CAS操作，这就是偏向锁。当线程竞争更激烈时，偏向锁就会升级为轻量级锁，轻量级锁认为虽然竞争是存在的，但是理想情况下竞争的程度很低，通过自旋方式等待一会儿上一个线程就会释放锁，但是当自旋超过了一定次数，或者一个线程持有锁，一个线程在自旋，又来了第三个线程访问时（反正就是竞争继续加大了），轻量级锁就会膨胀为重量级锁，重量级锁就是Synchronized,重量级锁会使除了此时拥有锁的线程以外的线程都阻塞。\n\n# 常用的锁\n\n## synchronized\nsynchronized锁是jvm内置的锁，不同于ReentrantLock锁。synchronized关键字可以修饰方法，也可以修饰代码块。synchronized关键字修饰方法时可以修饰静态方法，也可以修饰非静态方法；synchronized关键字可以修饰代码块。值得注意的是synchronized是一个对象锁，也就是它锁的是一个对象。因此，无论使用哪一种方法，synchronized都需要有一个锁对象。\n\n当修饰实例方法时，synchronized加锁的对象就是这个方法所在实例的本身;当修饰静态方法时synchronized加锁的对象为当前静态方法所在类的Class对象；当修饰代码块的时候，此时synchronized加锁对象即为传入的这个对象实例。\n\n需要注意在JDK1.6之后，JVM对synchronized进行了优化，有个锁升级的过程：无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁，这个升级只能是单向不可逆的。\n\n```java\n// synchronized 使用举例\nsynchronized (this) {\n    // todo\n}\n```",
                "contentImg": "http://49.232.129.253/files/1/article/img/2024-03-31_16-55-55_450341_9.png",
                "contentMemo": "Java常用锁的介绍与使用",
                "articleType": "13,45",
                "articleLabel": "7",
                "articleStatus": 1,
                "browseCount": 0,
                "likeCount": 0,
                "createTime": "2024-03-31 16:47:08",
                "updateTime": "2024-03-31 16:55:58",
                "articleIds": null,
                "pageSize": null,
                "pageNum": null,
                "type": null,
                "selectUser": null,
                "selectStatus": null,
                "sortType": null,
                "blogUser": {
                    "id": 1,
                    "username": "gszero",
                    "password": null,
                    "nickname": "GSZero",
                    "headImg": "https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg",
                    "email": "470687917@qq.com",
                    "status": 1,
                    "createTime": "2022-06-07 00:00:00",
                    "updateTime": "2022-06-07 02:02:03"
                },
                "articleTypes": [
                    {
                        "id": 13,
                        "parentId": 0,
                        "typeName": "JAVA",
                        "num": 4,
                        "node": 1,
                        "createUser": 1,
                        "createTime": "2023-04-17 23:15:20",
                        "updateTime": "2023-04-17 23:15:20"
                    },
                    {
                        "id": 45,
                        "parentId": 13,
                        "typeName": "锁",
                        "num": 1,
                        "node": null,
                        "createUser": 1,
                        "createTime": "2024-03-31 16:46:39",
                        "updateTime": "2024-03-31 16:46:39"
                    }
                ],
                "articleLabels": [
                    {
                        "id": 7,
                        "userId": 1,
                        "labelType": 1,
                        "labelName": "Java锁",
                        "articleNum": 1,
                        "createTime": "2024-03-31 16:50:40",
                        "updateTime": "2024-03-31 16:50:40"
                    }
                ]
            }],
  count: 0,
});
// 是否还有更多需要加载
const noMore = computed(() => article.list.length < article.count);
// 文章请求参数
const article_params = {
  pageNum: 1,
  pageSize: 5,
  type: 0,
  selectUser: 0,
  selectStatus: "1,2",
  sortType: "0,1",
};
// 是否可以执行加载中动画
const loading = ref(false);

/**
 * 加载下一页
 */
const load = () => {
  // getArticleListApi(article_params).then((res: any) => {
  //   if (res.code === 200) {
  //     article.list.push(...res.result.list);
  //     article.count = res.result.total;
  //     loading.value = false;
  //     article_params.pageNum = article_params.pageNum + 1;
  //   }
  // });
};
// 页面滚动事件
const scrollHandle = () => {
  const scrollHeight =
    document.body.scrollHeight || document.documentElement.scrollHeight;
  const scrollTop = document.body.scrollTop || document.documentElement.scrollTop;
  const clientHeight = document.documentElement.clientHeight;
  const distance = scrollHeight - scrollTop - clientHeight;
  if (distance <= 400 && noMore.value) {
    if (!loading.value) {
      loading.value = true;
      setTimeout(() => {
        load();
      }, 300);
    }
  }
};
onMounted(() => {
  CarouselData();
  load();
  // 监听滚动事件
  window.addEventListener("scroll", scrollHandle, false);
  setTimeout(() => {
    carouselLoading.value = false;
  }, 2000);
});
onUnmounted(() => {
  // 组件卸载时，停止监听
  window.removeEventListener("scroll", scrollHandle, false);
});
onActivated(() => {
  // store.setMenuIndex("1");
});
</script>

<style scoped lang="scss">
article {
  .carousel {
    margin-bottom: 15px;
    background-color: var(--el-bg-color-overlay);
  }

  .new {
    ul {
      list-style-type: none;
      padding: 0;
      margin: 0;
    }
  }

  .isLoading {
    padding: 30px;
    font-size: 30px;
  }
}
</style>
