<template>
  <div class="home">
    <div class="topbarBox">
      <mytabbar></mytabbar>
    </div>
    <div class="downBox">
      <div class="sidebarBox">
        <my-side-bar-vue></my-side-bar-vue>
      </div>
      <div class="contentBox">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script>
import Mytabbar from '@/components/Public/myTabbarVue.vue'
import MySideBarVue from '@/components/Public/mySideBarVue.vue'


export default {
  name: 'HomeView',
  data() {
    return {

    }
  },
  components: {
    Mytabbar,
    MySideBarVue
  },

}
</script>
<style lang="scss" scoped>
.home {
  box-sizing: border-box;

  .topbarBox {
    z-index: 1000;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 3.875rem;
  }

  .downBox {
    display: flex; // 使用 flex 布局
    margin-top: 3.875rem; // 确保顶部导航栏下方有空间

    .sidebarBox {
      position: fixed; // 侧边栏固定定位
      top: 3.875rem;
      left: 0;
      width: 12.5rem;
      height: calc(100% - 3.875rem); // 减去顶部导航栏的高度
      background-color: #545c64;

      display: flex;
      flex-direction: row;
      height: 100vh;

      /* 容器占满整个视口高度 */
    }

    .contentBox {
      
      flex: 1; // 内容区域占据剩余空间
      margin-left: 12.5rem; // 与侧边栏保持一定距离
      overflow-y: auto; // 如果内容超出，允许垂直滚动
      height: calc(100% - 3.875rem); // 减去顶部导航栏的高度
      // background-color: pink;
      // background-color:black;
      
    
    }
  }
}
</style>
