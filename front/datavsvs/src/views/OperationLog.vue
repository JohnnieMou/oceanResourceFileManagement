<template>
  <div class="home">
    <div class="topbarBox">
      <my-tabbar-vue></my-tabbar-vue>
    </div>
    <div class="Content-box">
      <div class="header">
        <h3>操作日志</h3>
      </div>
      <div class="Operation-Main-Area">
        <!-- 清除日志按钮 -->
        <div v-if="logData.length > 0" class="operationBox">
          <el-button type="danger" @click="onClearLogs" class="clearButton">清除日志</el-button>
        </div>

        <!-- 操作日志表格 -->
        <div v-if="logData.length === 0" class="empty-state">
          <p>暂无操作日志</p>
        </div>
        <el-table
          v-else
          :data="logData"
          style="width: 100%"
          v-loading="fetchLogLoading"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="operName" label="操作者" width="80" />
          <el-table-column prop="title" label="模块标题" />
          <el-table-column prop="operIp" label="主机地址" />
          <el-table-column prop="operParam" label="请求参数" :show-overflow-tooltip="true" />
          <el-table-column prop="jsonResult" label="返回结果" :show-overflow-tooltip="true" />
          <el-table-column prop="status" label="操作状态">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
                {{ scope.row.status === 0 ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="errorMsg" label="错误信息" :show-overflow-tooltip="true" />
          <el-table-column prop="operTime" label="操作时间">
            <template slot-scope="scope">
              {{ formatDate(scope.row.operTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="executeTime" label="执行时长(ms)" />
        </el-table>

        <!-- 自定义分页组件 -->
        <my-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          @update:page-size="handleSizeChange"
          @update:current-page="handleCurrentChange"
        />
      </div>
      <!-- 清除日志确认对话框 -->
      <el-dialog
        title="确认清除日志"
        :visible.sync="IsDialogVisible"
        @close="handleCancelClear"
        width="30%"
      >
        <span>您确定要清除所有操作日志吗？此操作不可恢复。</span>
        <span slot="footer" class="dialog-footer">
          <el-button @click="handleCancelClear">取消</el-button>
          <el-button type="primary" @click="clearLogs">确认</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import myTabbarVue from '@/components/Public/myTabbarVue.vue'
import myPagination from '@/components/Public/myPagination.vue'  // 引入自定义分页组件
import api from '@/api/api';
import dayjs from 'dayjs';

export default {
  components: {
    myTabbarVue,
    myPagination  // 注册分页组件
  },
  data() {
    return {
      fetchLogLoading: false,
      logData: [],
      total: 0,
      currentPage: 1,   // 当前页码
      pageSize: 10,     // 每页显示的条数
      queryParams: {
        page: 1,
        pageSize: 10
      },
      IsDialogVisible: false,  // 控制清除日志确认对话框的显示
    };
  },
  methods: {
    // 获取操作日志数据
    async getLogData() {
      try {
        this.fetchLogLoading = true;
        const response = await api.getOperationLogs(this.queryParams);

        if (response.code === 0) {
          this.logData = response.data.records;
          this.total = response.data.total;
        } else {
          this.$message.error(response.data.msg || '获取操作日志失败');
        }
      } catch (error) {
        console.error('获取操作日志时发生错误:', error);
        this.$message.error('获取操作日志时发生错误');
      } finally {
        this.fetchLogLoading = false;
      }
    },

    // 格式化日期
    formatDate(timestamp) {
      return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss');
    },

    // 处理每页显示数量变化
    handleSizeChange(val) {
      this.pageSize = val; // 更新每页显示条数
      this.queryParams.pageSize = val;  // 更新查询参数
      this.queryParams.page = 1;        // 重置为第一页
      this.getLogData();  // 重新加载数据
    },

    // 处理当前页变化
    handleCurrentChange(val) {
      this.currentPage = val;  // 更新当前页
      this.queryParams.page = val; // 更新查询参数
      this.getLogData();  // 重新加载数据
    },

    // 显示清除日志确认对话框
    onClearLogs() {
      this.IsDialogVisible = true;  // 显示对话框
    },

    // 取消清除操作
    handleCancelClear() {
      this.IsDialogVisible = false; // 关闭对话框
      this.$message({
        type: 'info',
        message: '已取消清除操作',
      });
    },

    // 清除操作日志
    async clearLogs() {
      // console.log('77');

      try {
        const response = await api.clearOperationLogs();
        if (response.data.code === 0) {
          this.$message.success('日志清除成功');
          this.getLogData(); // 重新加载日志数据
        } else {
          this.$message.error(response.data.msg || '清除日志失败');
        }
        this.IsDialogVisible = false; // 清除成功或失败后关闭对话框
      } catch (error) {
        console.error('清除日志时发生错误:', error);
        this.$message.error('清除日志时发生错误');
        this.IsDialogVisible = false; // 出现错误后关闭对话框
      }
    },
  },
  mounted() {
    this.getLogData(); // 组件挂载后获取日志数据
  },
};
</script>

<style lang="scss" scoped>
.home {
  box-sizing: border-box;

  .topbarBox {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 3.875rem;
    z-index: 999;
  }

  .header {
    width: 100%;
    text-align: left;
    h3 {
      margin: 0 !important;
    }
  }

  .Content-box {
    width: 100%;
    padding: 1.875rem;

    .Operation-Main-Area {
      .empty-state {
        text-align: center;
        color: #999;
        font-size: 16px;
        margin-top: 20px;
      }

      .operationBox {
        width: 100%;
        text-align: left;
        margin-bottom: 20px;

        .clearButton {
          margin: 1rem 0;
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
  }
}
</style>