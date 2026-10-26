<template>
  <el-dialog title="预览表" :visible="previewDialogVisible" width="80%" @close="closeDialog">
    <!-- 渲染表格，只需要表头 -->
    <el-table :data="[]" border>
      <!-- 动态渲染表格列 -->
      <el-table-column
        v-for="(column, index) in tableColumns"
        :key="index"
        :prop="index.toString()"
        :label="column"
      >
      </el-table-column>
    </el-table>

    <div slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">取消保存</el-button>
      <el-button type="primary" @click="confirmDialog">确认保存</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      previewTableData: null, // 预览表数据
      tableColumns: [], // 表格列配置
    };
  },
  props: {
    previewDialogVisible: {
      type: Boolean,
      required: true,
    },
    previewData: {
      type: Object,
      required: true,
    },
  },
  watch: {
    previewData(newVal) {
      if (newVal) {
        this.previewTableData = newVal;
        console.log('previewTableData:', this.previewTableData);

        // 动态生成表头
        this.tableColumns = Object.keys(this.previewTableData);
        console.log('this.tableColumns',this.tableColumns);
        
      }
    },
  },
  
  methods: {
    closeDialog() {
      this.$emit('close-preview');
    },
    confirmDialog() {
      this.$emit('confirm-preview');
    },
  },
};
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>