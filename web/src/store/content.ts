import { defineStore } from 'pinia'
import { ref } from 'vue'

export const contentStore = defineStore(
  'content',
  () => {
    let article = ref({})
    let docContent = ref({})

    function setArticle(article: any) {
      this.article = article
    }

    function setDocContent(docContent: any) {
      this.docContent = docContent
    }

    return {
      article,
      docContent,

      setArticle,
      setDocContent,
    }
  },
  {
    persist: [
      {
        storage: sessionStorage,
        pick: ['article', 'docContent'],
      }
    ],
  }
)
