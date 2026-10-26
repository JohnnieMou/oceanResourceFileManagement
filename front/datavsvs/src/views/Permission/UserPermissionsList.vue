<template>
    <div>
        <div class="home">
            <!-- 面包屑导航 -->
            <!-- <el-breadcrumb separator-class="el-icon-arrow-right" class="breadCrumb">
                <el-breadcrumb-item :to="{ path: '/usermanage' }">用户管理</el-breadcrumb-item>
                <el-breadcrumb-item>用户权限列表</el-breadcrumb-item>
            </el-breadcrumb> -->
            <div class="header">
                <h3>用户{{ currentUserName }}的权限列表</h3>
            </div>
            <!-- 权限列表展示 -->
            <div class="table">
                <div v-loading="MainTableDataLoading" class="permission-table-container">
                    <div class="empty-message" v-if="tableData.length === 0">
                        目前这里还没有任何数据哦，快去添加吧！
                    </div>
                    <!-- 添加权限按钮 -->
                    <div class="addPermissionButtonBox">
                        <el-button type="primary" @click="addPermissionToUser">添加权限</el-button>
                    </div>

                    <!-- 表格数据渲染 -->
                    <div v-if="tableData.length > 0">
                        <el-table :data="tableData" class="permission-table">
                            <!-- 表名列 -->
                            <el-table-column prop="tableCnName" label="表名" />
                            <el-table-column prop="yearStart" label="年份起始" />
                            <el-table-column prop="yearEnd" label="年份结束" />
                            <el-table-column prop="voyageStart" label="起始航号" />
                            <el-table-column prop="voyageEnd" label="结束航号" />
                            <el-table-column prop="createTime" label="权限创建时间" />
                            <el-table-column prop="updateTime" label="权限更新时间" />

                            <!-- 操作列 -->
                            <el-table-column label="操作" width="200" fixed="right">
                                <template v-slot="{ row }">
                                    <el-button size="mini" @click="editRecord(row)">编辑</el-button>
                                    <el-button size="mini" type="danger" @click="deleteRecord(row)">删除</el-button>
                                </template>
                            </el-table-column>
                        </el-table>

                        <!-- 分页组件 -->
                        <my-pagination :current-page="currentPage" :page-size="pageSize" :total="total"
                            @update:current-page="handleCurrentPageChange" @update:page-size="handlePageSizeChange" />
                    </div>
                </div>
            </div>
            <!-- 添加权限弹窗 -->
            <add-permisson-to-user :visible="permissionDialogVisible" :user-info="userInfo"
                @update:visible="permissionDialogVisible = $event" @add-permission="handleAddPermission">
            </add-permisson-to-user>

            <!-- 编辑权限弹窗 -->
            <edit-user-permission :visible="editDialogVisible" :user-info="userInfo" :current-row="currentRow"
                @update:visible="editDialogVisible = $event"
                @update-permission="fetchPermisionListById"></edit-user-permission>
        </div>
    </div>
</template>

<script>
import api from '@/api/api';
import MyPagination from '@/components/Public/myPagination.vue';
import AddPermissonToUser from '@/components/Permission/addPermissonToUser.vue';
import EditUserPermission from '@/components/Permission/editUserPermission.vue';

export default {
    components: { MyPagination, AddPermissonToUser, EditUserPermission },
    data() {
        return {
            MainTableDataLoading: false,
            total: 0,
            pageSize: 10,
            currentPage: 1,
            tableData: [], // 表格数据
            userPermissionId: '', // 用户Id,也是路由跳转Id
            currentUserName: '', // 当前用户的用户名，用于其他用途

            // 给用户添加权限start
            permissionDialogVisible: false,
            // 编辑权限弹窗的显示状态
            editDialogVisible: false,
            userInfo: '', // 传递给组件的用户信息
            currentRow: null, // 当前编辑的行数据
        };
    },
    created() {
        this.fetchUserId();
        this.fetchUserInfo();
        this.fetchPermisionListById();
    },
    methods: {

        // 给用户添加权限start
        addPermissionToUser() {
            this.permissionDialogVisible = true;
        },
        // 处理添加权限
        async handleAddPermission(params) {
            try {
                const response = await api.addPermission(params);
                if (response.data.code === 0) {
                    // this.$message.success('给用户添加权限成功');
                    this.fetchPermisionListById(); // 重新获取表格数据
                } else {
                    this.$message.error(response.data.msg);
                }
            } catch (error) {
                console.error('给用户添加权限失败:', error);
                // this.$message.error('给用户添加权限失败');
            }
        },
        // 给用户添加权限end

        // 获取用户信息start
        // 从路由的路径解码获取的表格信息
        fetchUserId() {
            // 路由参数是通过 query 传递的
            const userPermissionId = this.$route.query.userPermissionId;

            // 解码并赋值
            this.userPermissionId = decodeURIComponent(userPermissionId);

            // console.log("userPermissionId:", this.userPermissionId);
        },
        // 根据用户Id获取这个用户拥有的具体的用户信息
        async fetchUserInfo() {
            const response = await api.fetchUserInfoById(this.userPermissionId);
            if (response.data.code === 0 && response.data.data) {
                // console.log('根据用户Id拿到用户信息：', response);
                this.userInfo = response.data.data;
            } else {
                this.$message.error(response.data.msg || '获取当前用户信息失败');
            }
        },
        // 根据用户Id获取这个用户拥有的权限列表
        async fetchPermisionListById() {
            this.MainTableDataLoading = true;
            try {
                const response = await api.fetchPermissionListById(this.userPermissionId);
                if (response.data.code === 0) {
                    // console.log('当前用户的权限数据；', response);

                    // 确保 data 是一个数组
                    const data = response.data.data || [];

                    // 过滤掉用户名字段后，将数据存入 tableData
                    this.tableData = data.map(record => {
                        const { username, ...rest } = record;
                        // 提取用户名并存入 currentUserName
                        if (data.length > 0) {
                            this.currentUserName = username; // 提取用户名
                            // console.log('当前用户名:', this.currentUserName);
                        }
                        return rest;
                    });
                    // 设置总条数
                    this.total = data.length;
                }
            } catch (error) {
                console.error('获取当前用户的权限数据失败:', error);
                this.$message.error('获取当前用户的权限数据失败');
            } finally {
                this.MainTableDataLoading = false;
            }
        },


        handlePageSizeChange(val) {
            this.pageSize = val;
            this.currentPage = 1; // 重置当前页为第一页
            this.fetchPermisionListById();
        },
        handleCurrentPageChange(val) {
            this.currentPage = val;
            this.fetchPermisionListById();
        },

        // 获取用户信息end

        // 编辑操作
        editRecord(row) {
            this.currentRow = row; // 保存当前行数据
            this.editDialogVisible = true; // 打开编辑权限弹窗
        },
        // 删除权限start
        async deleteRecord(row) {
            try {
                const confirmResult = await this.$confirm('此操作将永久删除该权限, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                if (confirmResult === 'confirm') {
                    const params = {
                        userId: this.userPermissionId,
                        tableNames: [row.tableCnName]
                    };
                    const response = await api.deletePermission(params);
                    if (response.data.code === 0) {
                        this.$message.success('权限删除成功');
                        this.fetchPermisionListById(); // 重新获取表格数据
                    } else {
                        this.$message.error(response.data.msg);
                    }
                }
            } catch (error) {
                console.error('删除权限失败:', error);
                this.$message.error('删除权限失败');
            }
        }
          // 删除权限end
    }
}
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
            color: #303133;
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

        .addPermissionButtonBox {
            margin: 15px 0;
            width: 100%;
            display: flex;
        }

        .permission-table {
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