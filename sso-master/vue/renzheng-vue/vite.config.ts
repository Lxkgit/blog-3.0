import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
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
        },
   }

})
