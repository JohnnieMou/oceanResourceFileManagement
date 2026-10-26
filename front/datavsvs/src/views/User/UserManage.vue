<template>
  <div>
    <div class="home">
      <div class="header">
        <h3>用户管理</h3>
      </div>
      
      <div v-loading="MainTableDataLoading" class="table">
        <div class="empty-message" v-if="users.length === 0">
          目前这里还没有任何用户哦，快去注册吧！
        </div>

        <!-- 添加权限弹窗 -->
        <add-permisson-to-user :visible="permissionDialogVisible" :userInfo="userInfo"
          @update:visible="permissionDialogVisible = $event" @add-permission="handleAddPermission">
        </add-permisson-to-user>

        <!-- 编辑用户信息弹窗 -->
        <edit-user-info :visible="editDialogVisible" :userInfo="userInfo" @update:visible="editDialogVisible = $event"
          @update-user="handleUpdateUser">
        </edit-user-info>

        <!-- 用户表格 -->
        <div v-if="users.length > 0">
          <el-table :data="users" class="user-table">
            <!-- 用户ID -->
            <el-table-column prop="id" label="用户ID" width="80" />

            <!-- 姓名 -->
            <el-table-column prop="name" label="姓名" width="120" />

            <!-- 用户名 -->
            <el-table-column prop="username" label="用户名" width="150" />

            <!-- 角色 -->
            <el-table-column label="角色" width="120">
              <template v-slot="{ row }">
                {{ getRoleName(row.roleCode) }}
              </template>
            </el-table-column>

            <!-- 有效期开始时间 -->
            <el-table-column prop="validStartTime" label="有效期开始时间" width="180" />

            <!-- 有效期结束时间 -->
            <el-table-column prop="validEndTime" label="有效期结束时间" width="180" />

            <!-- 创建时间 -->
            <el-table-column prop="createTime" label="创建时间" width="180" />

            <!-- 最后登录时间 -->
            <el-table-column prop="lastLoginTime" label="最后登录时间" width="180" />

            <!-- 操作列 -->
            <el-table-column label="操作" width="450" fixed="right">
              <template v-slot="{ row }">
                <el-button size="mini" @click="IntoUserPermissionList(row)">权限详情</el-button>
                <el-button size="mini" @click="addPermissionToUser(row)">添加权限</el-button>
                <el-button size="mini" @click="editUser(row)">编辑用户</el-button>
                <el-button size="mini" type="danger" @click="deleteUser(row)">删除用户</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页组件 -->
          <my-pagination :current-page="currentPage" :page-size="pageSize" :total="total"
            @update:current-page="handleCurrentPageChange" @update:page-size="handlePageSizeChange" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/api/api';
import MyPagination from '@/components/Public/myPagination.vue';
import AddPermissonToUser from '@/components/Permission/addPermissonToUser.vue';
import EditUserInfo from '@/components/User/editUserInfo.vue';

export default {
  components: { MyPagination, AddPermissonToUser, EditUserInfo },
  data() {
    return {
      MainTableDataLoading: false,
      users: [], // 用户列表
      total: 0, // 总记录数
      pageSize: 10, // 每页显示条数
      currentPage: 1, // 当前页码

      // 给用户添加权限start
      permissionDialogVisible: false,
      userInfo: '', // 传递给组件的用户信息
      // 给用户添加权限end

      // 编辑用户信息start
      editDialogVisible: false, // 控制编辑弹窗的显示
      // 编辑用户信息end
    };
  },
  created() {
    this.fetchUserInfo();
  },
  methods: {
    // 进入权限详情start
    async IntoUserPermissionList(row) {
      // console.log('进入用户权限详情列表：', row.id);
      let userPermissionId = row.id;
      this.$router.push({
        path: `/userPermissionList/${userPermissionId}`,
        query: {
          userPermissionId: encodeURIComponent(userPermissionId),
        },
      });
    },
    // 进入权限详情end

    //   删除用户start
    async deleteUser(row) {
      try {
        let ids = row.id;
        // console.log('删除用户：',ids);

        const confirmResult = await this.$confirm('此操作将永久删除该用户, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });
        if (confirmResult === 'confirm') {
          const response = await api.deleteUser({ ids });
          if (response.data.code === 0) {
            this.$message.success('删除用户成功');
          
            this.fetchUserInfo();
            // 调用一个函数，将已删除用户的那一行的color标灰，操作也禁止，如何判断是否已删除，从users这个数据里遍历，如果是已经删除的用户，它的validEndTime是小于等于当前日期的（）
          } else {
            this.$message.error('删除用户失败: ' + response.data.msg);
          }
        }
      } catch (error) {
        this.$message.info('已取消删除');
      }
    },
    //   删除用户end

    // 编辑用户信息start
    editUser(row) {
      this.userInfo = row; // 将当前用户信息传递给编辑组件
      // console.log('this.userInfo:',this.userInfo);

      this.editDialogVisible = true; // 打开编辑弹窗
    },
    // 处理用户信息更新
    async handleUpdateUser(updatedUserInfo) {
      try {
        const response = await api.updateUserInfo(updatedUserInfo);
        if (response.data.code === 0) {
          this.$message.success('用户信息更新成功');
          this.editDialogVisible = false; // 关闭编辑弹窗
          this.fetchUserInfo(); // 刷新用户列表
        } else {
          this.$message.error(response.data.msg);
        }
      } catch (error) {
        console.error('更新用户信息失败:', error);
        this.$message.error('更新用户信息失败');
      }
    },
    // 编辑用户信息end

    // 给用户添加权限start
    addPermissionToUser(row) {
      this.userInfo = row;
      this.permissionDialogVisible = true;
    },
    // 处理添加权限
    async handleAddPermission(params) {
      try {
        const response = await api.addPermission(params);
        if (response.data.code === 0) {
          this.$message.success('给用户添加权限成功');
          this.getTableData(); // 重新获取表格数据
        } else {
          this.$message.error(response.data.msg);
        }
      } catch (error) {
        console.error('给用户添加权限失败:', error);
        this.$message.error('给用户添加权限失败');
      }
    },
    // 给用户添加权限end

    // 获取用户信息
    async fetchUserInfo() {
      this.MainTableDataLoading = true;
      try {
        const response = await api.fetchUserInfo(this.currentPage, this.pageSize);
        if (response.data.code === 0) {
          this.users = response.data.data.records; // 假设返回的数据在 records 中
          this.total = response.data.data.total; // 假设总记录数在 total 中
        } else {
          this.$message.error(response.data.msg);
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
        this.$message.error('获取用户信息失败');
      } finally {
        this.MainTableDataLoading = false;
      }
    },



    // 获取角色名称
    getRoleName(roleCode) {
      const roleMap = {
        1: '管理员',
        2: '普通用户',
        3: '游客',
      };
      return roleMap[roleCode] || '未知角色';
    },

    // 分页事件处理
    handleCurrentPageChange(val) {
      this.currentPage = val;
      this.fetchUserInfo();
    },

    handlePageSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1; // 重置为第一页
      this.fetchUserInfo();
    },
  },
};
</script>

<style lang="scss" scoped>
.home {
  box-sizing: border-box;
  padding: 1.875rem;

  .header {
    width: 100%;
    text-align: left;

    h3 {
      margin: 0 0 1.25rem 0 !important;
    }
  }

  .table {
    .empty-message {
      text-align: center;
      color: #999;
      font-size: 16px;
      margin-top: 20px;
      font-weight: bold;
    }

    .addPermissionToUserButtonBox {
      margin: 15px 0;
      width: 100%;
      display: flex;
    }

    .user-table {
      width: 100%;

      .el-table__header-wrapper {
        background-color: #eef1f6; // 表头背景颜色

        .el-table__header {
          tr {
            th {
              background-color: #eef1f6; // 表头单元格背景颜色
              color: #303133; // 表头文字颜色
              font-weight: bold; // 表头文字加粗
            }
          }
        }
      }
    }
  }
}
</style>