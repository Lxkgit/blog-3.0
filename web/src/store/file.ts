import { defineStore } from 'pinia'
import { ref } from 'vue'

export const fileStore = defineStore(
  'file',
  () => {
    let filePath = ref('null')
    let filePathArr = ref([''])
    let switchFlag = ref(true)

    return {
      filePath,
      filePathArr,
      switchFlag,
    }
  },
  {
    persist: [
      {
        storage: sessionStorage,
        pick: ['filePath', 'filePathArr', 'switchFlag'],
      },
    ],
  },
)
