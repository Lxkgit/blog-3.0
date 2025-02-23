//创建用户相关的小仓库
import {defineStore} from 'pinia'
//引入接口
import { reqAa,reqBb,reqCc } from '../../api/index';
 
 
//创建小仓库
let dianShangStore=defineStore('dianshang',{
        //小仓库存储数据的地方
        state:()=>{
           return {
                //从本地拿到token
                token: localStorage.getItem('TOKEN'),
                //当前登录人姓名
                name:'',
           }     
        },
        //异步| 逻辑处理的地方,在方法前面定义async，那么必须在拿到对应的方法前面加await 一起使用
        actions:{
            async getAa(){
                // //拿到登录后的结果
                // let res:any=await reqAa();
                // if(res){
                //     console.log(res)
                    
                //     //刷新token
                //     this.token=data;
                //     //把token放入本地
                //     localStorage.setItem('TOKEN',data);
                //     return 'ok';
                // }else{
                //     //失败返回失败对象
                //     return Promise.reject(Error('账号密码输入错误'))
                // }
                
            },
            async userInfo(){
                // //登录后获取用户信息 请求头自带token    
                // let res=await getUserInfo();
                // this.name=res.data.name;
            },    
            userLogout(){
                //退出登录 清空用户信息 和token
                this.token=''
                this.name=''
                localStorage.removeItem('TOKEN');
            },
            async useMyAuth(){
                // //登录后获取用户信息 请求头自带token    
                // let res=await getMyAuth();
                // return res.data;
            }, 
        },
        getters:{
 
        }
})
//对外暴露小仓库的方法
export default dianShangStore;