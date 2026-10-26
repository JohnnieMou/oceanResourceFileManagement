<template>
  <el-dialog :visible.sync="DialogVisible" title="字段确认" :modal="true" :lock-scroll="true" width="25%" >
    <div class="el-dialog__body">
         <!-- 选择Sheet下拉框 -->
    <!-- <el-select v-model="sheetValue" placeholder="请选择">
      <el-option v-for="item in sheetOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
    </el-select> -->

    <!-- 字段名字段类型选择表格 -->
    <el-table :data="fieldData">
      <!-- 修改字段名的显示 -->
      <el-table-column property="fieldName" label="字段名"></el-table-column>
      <el-table-column property="fieldType" label="字段类型"><!-- 修改字段类型-->
        <template #default="{ row }">
          <el-select v-model="row.fieldType" placeholder="请选择">
            <el-option v-for="item in fieldOptions" :key="item.value" :label="item.label"
              :value="item.value"></el-option>
          </el-select>
        </template>
      </el-table-column>
    </el-table>
    </div>
   

    <!-- 底部 -->
    <div slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">取 消</el-button>
      <el-button type="primary" @click="confirmFields" :loading="loading">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  props: {
    uploadType: String,
    fieldConfirmDialogVisible: Boolean,
    selectedConfirmSheet: String,
    fieldConfirmData: Object,
    loading: Boolean, 
  },
  data() {
    return {
      DialogVisible: this.fieldConfirmDialogVisible,
      fieldData: this.fieldConfirmData[this.selectedConfirmSheet] || [],
      sheetValue: this.selectedConfirmSheet,
      sheetOptions: Object.keys(this.fieldConfirmData).map(sheet => ({ value: sheet, label: sheet })),
      fieldOptions: [
        { value: 'string', label: 'string' },
        { value: 'number', label: 'number' },
        { value: 'date', label: 'date' },
      ],
    };
  },
  watch: {
    fieldConfirmDialogVisible(newVal) {
      this.DialogVisible = newVal;
    },
    selectedConfirmSheet(newSheet) {
      this.sheetValue = newSheet;
      this.fieldData = this.fieldConfirmData[newSheet] || [];
    },
    fieldData: {
      deep: true,
      handler(newValue) {
        this.$emit('update-field-data', this.selectedConfirmSheet, newValue);
        // this.$emit('update-field-data', this.sheetValue, newValue);
        // console.log('fieldData有改变,有改变字段类型',newValue);
        
      },
    },
  },
  methods: {
    closeDialog() {
      this.$emit('close-dialog');
    },

    confirmFields() {
      this.$emit('confirm-fields');
    },
  },
};
</script>

<style scoped>
.el-dialog__body {
        
        height: 18.75rem /* 300/16 */;
        /* 设置对话框内容的最大高度 */
        overflow-y: auto;
        align-content: center;
        /* 允许垂直滚动 */
    }
.dialog-footer {
  text-align: center;
}
</style>