<template>
  <div>
    <el-menu router :default-active="activePath" class="el-menu-demo1" mode="horizontal" background-color="#545c64"
      text-color="#fff" active-text-color="#409eff" @select="clickMenu" v-if="userRoleCode === 1">

      <el-menu-item :index="item.path" v-for="item in router" :key="item.path">{{ item.name }}</el-menu-item>
      <!-- <el-menu-item class="right" index="/login">登录</el-menu-item> -->

      <el-dropdown class="right" trigger="hover" @command="handleUserMethor">
        <span class="el-dropdown-link">
          <div class="welcometext">欢迎您</div>
          <div>{{ userInfo ? userInfo.name : '用户名' }}</div>
        </span>
        <template #dropdown>
          <el-dropdown-menu class="el-dropdown-menu" id="my-dropdown-menu">
            <el-dropdown-item disabled>姓名：{{ userInfo ? userInfo.name : '用户名' }}</el-dropdown-item>
            <el-dropdown-item disabled>用户名: {{ userInfo ? userInfo.username : '姓名' }}</el-dropdown-item>
            <el-dropdown-item disabled>
              角色：{{ getRoleName(userRoleCode) }}
              <!-- 角色：{{ userInfo ? userInfo.name : '用户名' }} -->
            </el-dropdown-item>
            <el-dropdown-item divided command="updateUserInfo">修改用户信息</el-dropdown-item>
            <el-dropdown-item command="updatePassword">修改密码</el-dropdown-item>

            <el-dropdown-item divided command="loginOut">退出</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-menu>
    <!-- 非管理员显示的盒子 -->
    <el-menu v-else router :default-active="activePath" class="el-menu-demo2" mode="horizontal"
      background-color="#545c64" text-color="#fff" active-text-color="#409eff" @select="clickMenu">
      <div class="tabbarBox">
        海洋信息管理系统
      </div>

      <el-dropdown class="right" trigger="hover" @command="handleUserMethor">
        <span class="el-dropdown-link">
          <div class="welcometext">欢迎您</div>
          <div>{{ userInfo ? userInfo.name : '用户名' }}</div>
        </span>
        <template #dropdown>
          <el-dropdown-menu class="el-dropdown-menu" id="my-dropdown-menu">
            <el-dropdown-item disabled>姓名：{{ userInfo ? userInfo.name : '用户名' }}</el-dropdown-item>
            <el-dropdown-item disabled>用户名: {{ userInfo ? userInfo.username : '姓名' }}</el-dropdown-item>
            <el-dropdown-item disabled>
              角色：{{ getRoleName(userRoleCode) }}
              <!-- 角色：{{ userInfo ? userInfo.name : '用户名' }} -->
            </el-dropdown-item>
            <el-dropdown-item divided command="updateUserInfo">修改用户信息</el-dropdown-item>
            <el-dropdown-item command="updatePassword">修改密码</el-dropdown-item>

            <el-dropdown-item divided command="loginOut">退出</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-menu>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" :visible.sync="updatePasswordDialogVisible" width="30%" append-to-body>
      <el-form :model="passwordForm">
        <el-form-item label="原密码">
          <el-input type="password" v-model="passwordForm.oldPassword"></el-input>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input type="password" v-model="passwordForm.newPassword"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="updatePasswordDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handlePasswordChange" :loading="isUpdatePasswordLoading">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 修改用户信息对话框 -->
    <el-dialog title="修改用户信息" :visible.sync="updateUserInfoDialogVisible" width="30%" append-to-body>
      <el-form :model="userInfoForm">
        <el-form-item label="用户名">
          <el-input v-model="userInfoForm.username"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userInfoForm.name"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="updateUserInfoDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleUserInfoChange" :loading="isUpdateUserInfoLoading">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api/api'
export default {
  data() {
    return {
      router: [
        { path: "/home", name: "首页", children: [] },
        { path: "/usermanage", name: "用户管理", children: [] },
        { path: "/OperationLog", name: "操作日志", children: [] }
        // { path: "/MapView", name: "地图", children: [] },
        // { path: "/permission", name: "权限管理", children: [] },
      ],
      activePath: this.$route.path, // 使用路由的path作为默认激活的菜单项

      // 修改密码start
      isUpdatePasswordLoading: false,
      updatePasswordDialogVisible: false,
      passwordForm: {
        oldPassword: '',
        newPassword: ''
      },
      // 修改密码end

      // 修改用户信息start
      userInfoForm: {
        id: '',
        username: '',
        name: '',
        roleCode: '',
        lastLoginTime: ''
      },
      updateUserInfoDialogVisible: false, // 控制修改用户信息对话框的显示
      isUpdateUserInfoLoading: false,
      userInfo: null,
      userRoleCode: '',
      userId: ''
      // 修改用户信息end
    };
  },
  mounted() {
    this.fetchUserInfo()
  },

  methods: {
    clickMenu(index, item) {
      console.log(item);

      this.$store.commit('updateActivePath', index); // 更新store中的状态
      this.$router.push(index); // 路由跳转
      if (index === '/home') {
        this.$store.commit('toggleSidebar', true); // 如果是首页，显示侧边导航栏
      } else {
        this.$store.commit('toggleSidebar', false); // 如果不是首页，隐藏侧边导航栏
      }
    },
    // 获取用户信息，先从token拿到当前用户Id，再通过接口获取具体的用户信息
    async fetchUserInfo() {
      const rawUserInfo = localStorage.getItem('userInfo');
      if (rawUserInfo) {
        const userInfo = JSON.parse(rawUserInfo);
        this.userId = userInfo.id;
        if (this.userId) {
          // console.log('当前用户Id已拿到:', this.userId);
          const response = await api.fetchUserInfoById(this.userId);
          if (response.data.code === 0 && response.data.data) {
            // console.log('根据用户Id拿到用户信息：', response);
            this.userInfo = response.data.data; // 使用 JSON.parse 转换字符串回对象
            this.userRoleCode = this.userInfo.roleCode
          } else {
            this.$message.error(response.data.msg || '获取当前用户信息失败');
          }
        }
      } else {
        console.log('未找到用户信息');
      }
    },
    // 获取角色名称
    getRoleName(roleCode) {

      // console.log('roleCode:', roleCode);

      const roleMap = {
        1: '管理员',
        2: '普通用户',
        3: '游客',
      };
      return roleMap[roleCode] || '普通用户'; // 默认返回普通用户
    },
    // 修改密码start
    handleUserMethor(command) {
      if (command === 'updatePassword') {
        // console.log('点击了修改密码');
        this.updatePasswordDialogVisible = true;
      } else if (command === 'updateUserInfo') {
        this.userInfoForm.id = this.userInfo ? this.userInfo.id : '';
        this.userInfoForm.username = this.userInfo ? this.userInfo.username : '';
        this.userInfoForm.name = this.userInfo ? this.userInfo.name : '';
        // console.log('点击了修改用户信息');
        this.updateUserInfoDialogVisible = true;
      } else if (command === 'loginOut') {
        // 从缓存从清除登录信息
        localStorage.removeItem('userInfo');
        this.$router.push({
          path: '/login',
        });
      }


    },
    async handlePasswordChange() {
      // 在这里处理密码更改逻辑
      // 从表单获取新旧密码
      const { oldPassword, newPassword } = this.passwordForm;
      this.isUpdatePasswordLoading = true; // 开始加载，可以显示加载中的动画或提示

      try {
        const res = await api.updatePassword({
          oldPassword,
          newPassword
        });

        if (res.data.code === 0) {
          // 密码更新成功
          this.$confirm('密码更新成功', '提示', {
            confirmButtonText: '确定',
            type: 'success',
            cancelButtonText: '',
            showCancelButton: false,
          }).then(() => {
            // 清空对话框并关闭
            this.passwordForm.oldPassword = '';
            this.passwordForm.newPassword = '';
            this.updatePasswordDialogVisible = false;
          });
        } else {
          // 密码更新失败，服务器返回错误信息
          this.$confirm(res.data.msg, '错误', {
            confirmButtonText: '确定',
            type: 'error',
            cancelButtonText: '',
            showCancelButton: false,
          });
        }
      } catch (error) {
        // 请求失败，可能是网络问题或其他错误
        this.$confirm('密码更新失败，请检查网络或稍后再试', '错误', {
          confirmButtonText: '确定',
          type: 'error',
          cancelButtonText: '',
          showCancelButton: false,
        });
      } finally {
        this.isUpdatePasswordLoading = false; // 结束加载
      }
    },
    // 修改密码end

    // 修改用户信息start
    async handleUserInfoChange() {
      // 在这里处理用户信息更改逻辑
      // 从表单获取用户名和真实姓名
      const { username, name, id } = this.userInfoForm;


      this.isUpdateUserInfoLoading = true; // 开始加载

      try {
        const res = await api.updateUserInfo({
          id,
          username,
          name
        });

        if (res.data.code === 0) {
          // 用户信息更新成功
          this.$confirm('用户信息更新成功', '提示', {
            confirmButtonText: '确定',
            type: 'success',
            cancelButtonText: '',
            showCancelButton: false,
          }).then(() => {
            // 清空对话框并关闭
            this.userInfoForm.username = '';
            this.userInfoForm.name = '';
            this.updateUserInfoDialogVisible = false;
            this.fetchUserInfo();
          });
        } else {
          // 用户信息更新失败，服务器返回错误信息
          this.$confirm(res.data.msg, '错误', {
            confirmButtonText: '确定',
            type: 'error',
            cancelButtonText: '',
            showCancelButton: false,
          });
        }
      } catch (error) {
        // 请求失败，可能是网络问题或其他错误
        this.$confirm('用户信息更新失败，请检查网络或稍后再试', '错误', {
          confirmButtonText: '确定',
          type: 'error',
          cancelButtonText: '',
          showCancelButton: false,
        });
      } finally {
        this.isUpdateUserInfoLoading = false; // 结束加载
      }
    }
    // 修改用户信息end
  },
  watch: {
    '$route.path'(newPath) {
      this.activePath = newPath; // 监听路由变化，更新激活的菜单项
    }
  }
}
</script>

<style lang="scss" scoped>
.el-menu-demo1 {
  height: 60px; 

  .right {
    float: right;
    color:#fff;
    margin-right: 20px; // 设置右边距
    box-sizing: border-box;
    text-align: center;
    font-size: 0.875rem; // 14px / 16
    display: flex; // 使用flex布局
    flex-direction: column; // 设置主轴方向为垂直
    justify-content: center; // 垂直居中
    align-items: center; // 水平居中
    height: 100%; // 设置高度为100%，使其填充父元素的高度
    // width: 25rem /* 400/16 */;
    position: relative;

  }

  .welcometext {
    margin-bottom: 10px; // 在“欢迎您”和“用户名”之间添加间距
  }
}


.el-menu-demo2 {
  height: 60px;
  position: relative;

  .tabbarBox {
    width: 12.5rem
      /* 200/16 */
    ;
    height: 100%;
    // background-color: red;
    // text-align: center;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #fff;
  }

  .right {
    position: absolute;
    top: -3.75rem;
    float: right;
    color: #fff;
    margin-right: 20px; // 设置右边距
    box-sizing: border-box;
    text-align: center;
    font-size: 0.875rem; // 14px / 16
    display: flex; // 使用flex布局
    flex-direction: column; // 设置主轴方向为垂直
    justify-content: center; // 垂直居中
    align-items: center; // 水平居中
    height: 100%; // 设置高度为100%，使其填充父元素的高度
    // width: 25rem /* 400/16 */;
    position: relative;

  }

  .welcometext {
    margin-bottom: 10px; // 在“欢迎您”和“用户名”之间添加间距
  }


}



// .el-menu::after, .el-menu::before {
//   display: table;
//   content: none; /* 使用 none 替代空字符串 */
// }


//修改密码start
// .updatePasswordDialog{
//   width: 600px;
//   height: 600px;
//   background-color: red;
// }
// //修改密码end

// .top-nav{
//     width: 100%;
//     height: 5rem /* 80/16 */;
//     background-color: skyblue;
//     display: flex;
//     justify-content: space-between;
//     .left{
//         height: 100%;
//         width: 18.75rem /* 300/16 */;
//         display: flex;
//         justify-content: center;
//         align-items: center;

//     }
// }
</style>