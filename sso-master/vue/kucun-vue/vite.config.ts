import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve:{
    alias:{
      //相对路径别名配置 用@符号替代src
      "@": path.resolve("./src")
    }
  },
  server: {
    // 跨域的写法
        proxy: {
          '/auth': {
            // 实际请求地址
            target: 'http://auth-server:8084/', 
            changeOrigin: true,
            //把api前置去掉
            rewrite: (path) => path.replace(/^\/auth/, ""),
          },
          '/res': {
            // 实际请求地址
            target: 'http://res-server:8085/', 
            changeOrigin: true,
            //把api前置去掉
            rewrite: (path) => path.replace(/^\/res/, ""),
          },
          '/ds': {
            // 实际请求地址
            target: 'http://dianshang:8086/', 
            changeOrigin: true,
            //把api前置去掉
            rewrite: (path) => path.replace(/^\/ds/, ""),
          },
          '/kc': {
            // 实际请求地址
            target: 'http://kucun:8087/', 
            changeOrigin: true,
            //把api前置去掉
            rewrite: (path) => path.replace(/^\/kc/, ""),
          },
        },
   }

})
