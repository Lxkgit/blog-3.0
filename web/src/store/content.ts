import { defineStore } from 'pinia'

export const contentStore = defineStore(
  'content',
  () => {
    let article = {}
    let docContent = {}

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
      },
    ],
  },
)
