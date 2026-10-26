<template>
  <el-menu router :default-active="activePath" class="el-menu-vertical-demo" background-color="#545c64"
          text-color="#fff" active-text-color="#409eff">
    <el-menu-item index="/home">
      <span slot="title">选择表</span>
    </el-menu-item>
    <el-menu-item index="/createTable">
      <span slot="title">创建表</span>
    </el-menu-item>
    <el-menu-item v-if="userRoleCode === 1 || userRoleCode === 2" index="/noteBookList">
      <span slot="title">记事本</span>
    </el-menu-item>
    <el-menu-item index="/manageTags">
      <span slot="title">管理标签</span>
    </el-menu-item>
  </el-menu>
</template>

<script>
import api from '@/api/api'; // 确保引入正确的API模块

export default {
  data() {
    return {
      activePath: this.$store.state.activePath, // 使用store中的状态作为默认激活的菜单项
      userRoleCode: '', // 用于存储用户的角色代码
      userId: '' // 用于存储用户的ID
    };
  },
  watch: {
    "$store.state.activePath"(newValue) {
      this.activePath = newValue; // 监听store中的状态变化，更新激活的菜单项
    }
  },
  computed: {
    isSidebarVisible() {
      return this.$store.state.isSidebarVisible;
    }
  },
  mounted() {
    this.fetchUserInfo(); // 在组件挂载时获取用户信息
  },
  methods: {
    // 获取用户信息
    async fetchUserInfo() {
      const rawUserInfo = localStorage.getItem('userInfo');
      if (rawUserInfo) {
        const userInfo = JSON.parse(rawUserInfo);
        this.userId = userInfo.id;
        if (this.userId) {
          const response = await api.fetchUserInfoById(this.userId);
          if (response.data.code === 0 && response.data.data) {
            this.userRoleCode = response.data.data.roleCode; // 获取用户角色代码
          } else {
            this.$message.error(response.data.msg || '获取当前用户信息失败');
          }
        }
      } else {
        console.log('未找到用户信息');
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.el-menu-vertical-demo {
  overflow-x: hidden;
  width: 12.5rem /* 200/16 */;
  z-index: 1999;

  .button {
    margin-top: 1.25rem /* 20/16 */;
    width: 9.375rem /* 150/16 */;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}

.el-menu-vertical-demo {
  overflow-x: hidden;
  overflow-y: auto;
  /* 允许竖向滚动 */
  width: 12.5rem;
  flex-shrink: 0;
  /* 防止侧边栏在内容过多时缩小 */
}
</style>