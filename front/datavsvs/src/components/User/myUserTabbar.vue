<template>
    <div>
        <el-container>

            <div class="user-info" @click="toggleSidebar">
                <span class="username">用户名</span>
                <i :class="isCollapse ? 'el-icon-arrow-left' : 'el-icon-arrow-right'" class="toggle-icon"></i>
            </div>


            <el-aside :class="{ 'is-collapse': isCollapse }">
                用户信息
                <el-menu :default-active="activeIndex" class="el-menu-vertical-demo" @open="handleOpen"
                    @close="handleClose" :collapse="isCollapse">
                    <el-submenu index="1">
                        <!-- <i class="el-icon-menu"></i> -->
                        <span>导航二</span>
                    </el-submenu>
                    <el-menu-item index="2">
                        <!-- <i class="el-icon-menu"></i> -->
                        <span>导航二</span>
                    </el-menu-item>


                </el-menu>
            </el-aside>

        </el-container>
    </div>
</template>
<script>
export default {
    data() {
        return {
            isCollapse: false,
            activeIndex: '1'
        };
    },
    methods: {
        toggleSidebar() {
            this.isCollapse = !this.isCollapse;
        },
        handleOpen(key, keyPath) {
            console.log(key, keyPath);
        },
        handleClose(key, keyPath) {
            console.log(key, keyPath);
        }
    }
};
</script>
<style scoped>
.el-header {
    background-color: #333;
    color: #fff;
    text-align: center;
    line-height: 60px;
}

.user-info {
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.user-icon {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    margin-right: 10px;
}

.username {
    font-size: 16px;
}

.toggle-icon {
    font-size: 20px;
}

.el-aside {
    transition: width 0.3s ease;
    width: 200px;
}

.is-collapse {
    width: 600px;
    /* height: 100%;
    background-color: pink; */
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 0px;
    min-height: 400px;
}

.el-menu-vertical-demo .el-submenu__title i {
    margin-right: 8px;
}
</style>