<template>
    <div>
      <div class="home">
        <!-- 废弃页面 -->
         <h1>废弃页面</h1>
        <div v-loading="MainTableDataLoading" class="table">
          <div class="empty-message" v-if="tableData.length === 0">
            目前这里还没有任何数据哦，快去添加吧！
          </div>
  
          <!-- 添加权限按钮 -->
          <div class="addPermissionButtonBox">
            <el-button type="primary" @click="addPermission">添加权限</el-button>
          </div>
  
          <!-- 添加权限弹窗 -->
          <add-permission
            :visible="permissionDialogVisible"
            @update:visible="permissionDialogVisible = $event"
            :editing="editing"
            :editData="editData"
            @add-permission="handleAddPermission"
            @edit-permission="handleEditPermission"
          />
  
          <!-- 表格数据渲染 -->
          <div v-if="tableData.length > 0">
            <el-table :data="tableData" class="permission-table">
              <!-- 动态渲染表格列 -->
              <el-table-column
                v-for="(column, index) in tableColumns"
                :key="index"
                :prop="Object.keys(tableData[0])[index]"
                :label="column"
              />
  
              <!-- 操作列 -->
              <el-table-column label="操作" width="200" fixed="right">
                <template v-slot="{ row }">
                  <el-button size="mini" @click="editRecord(row)">编辑</el-button>
                  <el-button size="mini" type="danger" @click="deleteRecord(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
  
            <!-- 分页组件 -->
            <my-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              @update:current-page="handleCurrentPageChange"
              @update:page-size="handlePageSizeChange"
            />
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import api from '@/api/api';
  import MyPagination from '@/components/Public/myPagination.vue';
  import AddPermission from '@/components/Test/addPermission.vue';
  
  export default {
    components: { MyPagination, AddPermission },
    data() {
      return {
        MainTableDataLoading: false,
        total: 0,
        pageSize: 10,
        currentPage: 1,
        tableData: [], // 表格数据
        tableColumns: [], // 表头列
        // treeTableData: [], // 树形表格数据（已废弃）
  
        // 添加权限start
        permissionDialogVisible: false, // 添加权限弹窗是否可见
        editing: false, // 是否处于编辑模式
        editData: null, // 编辑时的原始数据
        // 添加权限end
      };
    },
    created() {
      this.getTableData();
    },
    methods: {
      // 数据更新方法
      updatetableData(data) {
        const records = data.records || [];
        if (records.length > 0) {
          // 定义中文表头映射
          const chineseHeaders = {
            username: '用户名',
            tableCnName: '表名',
            yearStart: '年份起始',
            yearEnd: '年份结束',
            voyageStart: '起始航号',
            voyageEnd: '结束航号',
            createTime: '权限创建时间',
            updateTime: '权限更新时间',
          };
  
          // 使用映射生成表头
          this.tableColumns = Object.keys(records[0]).map((key) => chineseHeaders[key] || key);
  
          // 所有记录都作为数据展示
          this.tableData = records;
        }
      },
      async getTableData() {
        this.MainTableDataLoading = true;
        try {
          const response = await api.fetchPermission(this.currentPage, this.pageSize);
          // console.log('权限列表：', response);
  
          if (response.data.code === 0) {
            const data = response.data.data;
            this.total = data.total;
            this.updatetableData(data);
          }
        } catch (error) {
          console.error('获取权限数据失败:', error);
          this.$message.error('获取数据失败');
        } finally {
          this.MainTableDataLoading = false;
        }
      },
      handleFilterResults(data) {
        console.log('查询出来的结果：', data);
        this.tableData = data.records;
        this.total = data.total;
      },
      handlePageSizeChange(val) {
        this.pageSize = val;
        this.currentPage = 1; // 重置当前页为第一页
        this.getTableData();
      },
      handleCurrentPageChange(val) {
        this.currentPage = val;
        this.getTableData();
      },
      // 编辑操作
      editRecord(row) {
        console.log('编辑记录:', row);
        this.editing = true;
        this.editData = row;
        this.permissionDialogVisible = true;
      },
      // 删除操作
      deleteRecord(row) {
        console.log('删除记录:', row);
        // 这里可以添加删除逻辑
      },
      // 添加权限start
      // 添加权限
      addPermission() {
        this.editing = false;
        this.permissionDialogVisible = true;
      },
      // 处理添加权限
      async handleAddPermission(params) {
        try {
          const response = await api.addPermission(params);
          if (response.data.code === 0) {
            this.$message.success('权限添加成功');
            this.getTableData(); // 重新获取表格数据
          } else {
            this.$message.error(response.data.msg);
          }
        } catch (error) {
          console.error('添加权限失败:', error);
          this.$message.error('添加权限失败');
        }
      },
      // 处理编辑权限
      async handleEditPermission(params) {
        try {
          const response = await api.updatePermission(params);
          if (response.data.code === 0) {
            this.$message.success('权限更新成功');
            this.getTableData(); // 重新获取表格数据
          } else {
            this.$message.error(response.data.msg);
          }
        } catch (error) {
          console.error('编辑权限失败:', error);
          this.$message.error('编辑权限失败');
        }
      },
      // 添加权限end
    },
  };
  </script>
  
  <style lang="scss" scoped>
  .home {
    box-sizing: border-box;
    padding: 1.875rem;
  
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