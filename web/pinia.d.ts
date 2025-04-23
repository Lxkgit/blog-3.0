// src/types/pinia.d.ts
import 'pinia'
 
declare module 'pinia' {
  export interface DefineStoreOptionsBase<S, Store> {
    // 声明 persist 配置项
    persist?: {
      enabled?: boolean
      strategies?: Array<{
        key?: string
        storage?: Storage
        paths?: string[]
      }>
    }
  }
}