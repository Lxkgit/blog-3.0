import { defineStore } from "pinia";


export const tagsStore = defineStore('tag', () => {

    let tags = [
        {
            active: true,  // 是否激活
            title: '个人中心', // 任务栏标题
            close: false,  // 是否可以关闭
            path: '/admin/index', // 当前标签的路径
            open: 0, // 标签打开方式 0：通过左侧菜单打开 1： 通过页面打开
        },
    ]
    let sideBar = false
    // 当前激活的tag
    let selectedTag = 0

    function addTag(name: any, path: any, add?: any) {
        let flag = false
        for (let i = 0; i < this.tags.length; i++) {
            if (this.tags[i].path === path) {
                flag = true;
                this.tags[i].active = true;
            }
        }

        if (!flag) {
            if (add !== undefined && add === true) {
                let oldTags = this.tags
                this.tags = []
                for (let i = 0; i < oldTags.length; i++) {
                    if (oldTags[i].active === false) {
                        this.tags.push(oldTags[i])
                    } else {
                        oldTags[i].active = false
                        this.tags.push(oldTags[i])
                        this.tags.push({ active: true, title: name, close: true, path: path, open: 1 })
                    }
                }
            } else {
                this.tags.map(item => (item.active = false))
                this.selectedTag = this.tags.length
                this.tags.push({ active: true, title: name, close: true, path: path, open: 0 })
            }
        }
    }
    function delTag(index: any, nextIndex?: any) {
        if (this.tags[index].close) {
            if (this.tags[index].active) {
                this.tags.splice(index, 1)
                this.tags[nextIndex].active = true
                this.selectedTag = nextIndex
            } else {
                this.tags.splice(index, 1)
            }
        }
    }
    function delTagByPath(path: any) {
        for (let i = 0; i < this.tags.length; i++) {
            if (this.tags[i].path === path) {
                if (this.tags[i].close) {
                    this.tags.splice(i, 1)
                }
            }
        }
    }
    function activeTag(path: any) {
        this.tags.map(item => (item.active = false));
        for (let i = 0; i < this.tags.length; i++) {
            if (this.tags[i].path === path) {
                this.tags[i].active = true;
                this.selectedTag = i;
            }
        }
    }
    function delAllTags() {
        this.tags.splice(1, this.tags.length - 1)
    }
    function delOtherTags() {
        for (let i = 0; i < this.tags.length; i++) {
            if (!this.tags[i].active && this.tags[i].close) {
                this.tags.splice(i, 1)
                i--
            }
        }
    }
    function saveTags() {
        sessionStorage.setItem('tags', JSON.stringify(this.tags))
    }

    function restoreTags() {
        const tagsJSON: any = sessionStorage.getItem('tags')
        if (tagsJSON != null) {
            this.tags = tagsJSON
        }

    }
}, {
    persist: {
        storage: sessionStorage
        // enabled: true,
        // strategies: [
        //     {
        //         key: 'tags',
        //         storage: sessionStorage,
        //         paths: ['tags']
        //     }
        // ]
    }
})