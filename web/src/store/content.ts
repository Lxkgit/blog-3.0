import { defineStore } from "pinia";


export const contentStore = defineStore('content', {
    state: () => ({
        article: {},
        docContent: {}
    }),
    actions: {
       setArticle(article: any) {
            this.article = article
       },
       setDocContent(docContent: any) {
            this.docContent = docContent
       }
    },
    persist: {
        enabled: true,
        storage: localStorage
    }
})
