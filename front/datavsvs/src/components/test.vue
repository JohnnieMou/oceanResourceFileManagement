<template>
    <div class="operate-container">
      <!-- 上传Excel文件 -->
      <el-dropdown @command="handleUpload">
        <el-button type="primary" :loading="IsUploadExcelLoading">
          上传Excel文件
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="cover">覆盖上传</el-dropdown-item>
            <el-dropdown-item :disabled="!hasExistingData" command="append">追加上传</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
  
      <input type="file" ref="fileInput" style="display: none" @change="onFileSelected" accept=".xlsx, .xls">
  
      <!-- 预览表组件 -->
      <myreview-dialog
        :previewDialogVisible="previewDialogVisible"
        :preview-data="previewData"
        @close-preview="closePreviewDialog"
        @confirm-preview="confirmPreview"
      />
  
      <!-- 字段确认组件 -->
      <my-dialog
        :uploadType="uploadType"
        :fieldConfirmDialogVisible="fieldConfirmDialogVisible"
        :selectedConfirmSheet="selectedConfirmSheet"
        :fieldConfirmData="fieldConfirmData"
        :loading="loading"
        @close-dialog="closeDialog"
        @confirm-fields="confirmFields"
        @update-field-data="updateFieldData"
      />
    </div>
  </template>
  
  <script>
  import api from '@/api/api';
  import myDialog from './myDialog.vue';
  import MyreviewDialog from './myReviewDialog.vue';
  
  export default {
    components: { myDialog, MyreviewDialog },
    props: {
      tableName: {
        type: String,
        required: true,
      },
      hasExistingData: {
        type: Boolean,
        required: false,
      },
    },
    data() {
      return {
        IsUploadExcelLoading: false,
        fileInput: null,
        uploadType: '',
        fieldConfirmDialogVisible: false,
        fieldConfirmData: {},
        selectedConfirmSheet: '',
        loading: false,
        previewDialogVisible: false, // 预览表对话框的显示状态
        previewData: {}, // 预览表数据
      };
    },
    methods: {
      // 上传操作
      handleUpload(command) {
        this.IsUploadExcelLoading = true;
        this.uploadType = command;
        this.$refs.fileInput.click();
      },
  
      // 处理文件选择
      async onFileSelected(event) {
        const file = event.target.files[0];
        if (!file) {
          this.$message.info('未选择文件，已取消上传');
          this.IsUploadExcelLoading = false;
          return;
        }
        const formData = new FormData();
        formData.append('excel', file);
  
        try {
          const { tableName } = this;
          const response = await api.uploadConfirm(formData, tableName);
         
  
          if (response.data.code === 0) {
            
            this.previewData = JSON.parse(JSON.stringify(response.data.data.Sheet1));
            // console.log('上传字段类型：', this.previewData);
            this.previewDialogVisible = true; // 显示预览表对话框
          } else {
            this.$message(response.data.msg || '上传失败');
          }
        } catch (error) {
          this.$message('上传失败');
        } finally {
          this.IsUploadExcelLoading = false;
        }
  
        // 重置 file input
        if (this.$refs.fileInput) {
          this.$refs.fileInput.value = '';
        }
      },
  
      // 关闭预览表对话框
      closePreviewDialog() {
        this.previewDialogVisible = false;
        this.previewData = {};
      },
  
      // 确认预览表
      confirmPreview() {
        this.previewDialogVisible = false;
  
        // 将预览数据传递给字段确认组件
        this.fieldConfirmData = Object.fromEntries(
          Object.entries(this.previewData).map(([sheetName, fields]) => [
            sheetName,
            Object.entries(fields).map(([fieldName, fieldType]) => ({
              fieldName,
              fieldType,
            })),
          ])
        );
        this.selectedConfirmSheet = Object.keys(this.fieldConfirmData)[0];
  
        if (this.uploadType === 'cover') {
          this.fieldConfirmDialogVisible = true; // 弹出字段确认对话框
        } else if (this.uploadType === 'append') {
          // 调用追加上传接口
          api.uploadAppend(this.tableName)
            .then(() => {
              this.$message.success('追加上传成功');
              window.location.reload(); // 刷新页面
            })
            .catch(() => {
              this.$message.error('追加上传失败');
            });
        }
      },
  
      // 关闭字段确认对话框
      closeDialog() {
        this.fieldConfirmDialogVisible = false;
      },
  
      // 确认字段类型提交
      async confirmFields() {
        try {
          this.loading = true;
          const fieldListMap = Object.fromEntries(
            Object.entries(this.fieldConfirmData).map(([sheetName, fields]) => [
              sheetName,
              Object.fromEntries(fields.map(field => [field.fieldName, field.fieldType])),
            ])
          );
  
          if (this.uploadType === 'cover') {
            const { tableName } = this;
            await api.uploadCover(fieldListMap, tableName);
          }
  
          this.fieldConfirmDialogVisible = false;
          this.$message.success('所有sheet的字段类型已确认');
          window.location.reload(); // 刷新页面
        } catch (error) {
          this.$message('确认字段类型失败');
        } finally {
          this.loading = false;
        }
      },
  
      // 更新字段数据
      updateFieldData(sheet, updatedFields) {
        this.fieldConfirmData[sheet] = updatedFields;
      },
    },
  };
  </script>
  
  <style scoped>
  .operate-container {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
    align-items: center;
  }
  </style>