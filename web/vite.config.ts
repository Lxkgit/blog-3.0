import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from "path"

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      "@": resolve(__dirname, "src")
    },
  },
  server: {
    // https: {
    //   // cert
    // },
    proxy: {
      '/api': {
        // target: 'http://localhost:60001',
        target: 'http://192.168.200.128:60001',
        changeOrigin: true,
        secure: false,
        // protocolRewrite: "https",
        rewrite: (path) => path.replace(/^\/api/, ''),
      }
    },
  }
})
