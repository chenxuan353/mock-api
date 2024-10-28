function setIframeHeight(iframe, callback) {
    if (iframe) {
        var iframeWin = iframe.contentWindow || iframe.contentDocument.parentWindow;
        var height = 0;
        if (iframeWin.document.body) {
            height = (iframeWin.document.documentElement.getBoundingClientRect().height || iframeWin.document.body.getBoundingClientRect().height);
        }
        if (height < iframe.parentElement.parentElement.getBoundingClientRect().height) {
            height = iframe.parentElement.parentElement.getBoundingClientRect().height;
        }
        height = parseInt(height);
        var ppscrollHeight = iframe.parentElement.parentElement.scrollHeight;
        if (height < ppscrollHeight) {
            height += 0.5;
        }
        iframe.style.height = height + "px";
        iframe.parentElement.style.height = ppscrollHeight + "px";
    }
    if(callback){
        callback();
    }
};
function resetIframeHeight(callback) {
    iframe = document.getElementById('external-frame');
    if (iframe) {
        iframe.style.height = "auto";
        iframe.parentElement.style.height = "auto";
    }
    setTimeout(() => { setIframeHeight(iframe, callback) }, 50);
};
window.addEventListener('resize', () => { resetIframeHeight() });

new Vue({
    el: '#app',
    data() {
        return {
            isCollapse: false,
            selectKey: 0,
            menuList: [
                {
                    title: "模块",
                    icon: "el-icon-menu",
                    bindKey: "module",
                    path: "module.html",
                },
                {
                    title: "注册表",
                    icon: "el-icon-cpu",
                    bindKey: "register",
                    path: "register.html",
                },
                {
                    title: "响应树",
                    icon: "el-icon-cpu",
                    bindKey: "groupTree",
                    path: "groupTree.html",
                },
                {
                    title: "管理",
                    icon: "el-icon-s-operation",
                    bindKey: "manage",
                    path: "manage.html",
                },
                {
                    title: "关于",
                    icon: "el-icon-s-promotion",
                    bindKey: "about",
                    path: "about.html",
                }
            ],
        };
    },
    mounted() {
        if (location.hash) {
            for (let i in this.menuList) {
                if ("#" + this.menuList[i].bindKey == location.hash) {
                    this.selectKey = i;
                    document.title = 'Mock - ' + this.menuList[i].title
                    break;
                }
            }
        }
        resetIframeHeight();
    },
    methods: {
        menuSelect(index) {
            this.selectKey = index;
            location.hash = this.menuList[this.selectKey].bindKey
            document.title = 'Mock - ' + this.menuList[this.selectKey].title
        },
    },
    computed: {
        collapseIcon: function () {
            return this.isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold';
        },
        title: function () {
            return this.menuList[this.selectKey]['title']
        },
        iframeSrc: function () {
            return this.menuList[this.selectKey]['path']
        }
    }
})

