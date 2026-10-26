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

    <!-- 传递数据给子组件 -->
    <my-dialog :uploadType="uploadType" :fieldConfirmDialogVisible="fieldConfirmDialogVisible"
      :selectedConfirmSheet="selectedConfirmSheet" :fieldConfirmData="fieldConfirmData" :loading="loading"
      @close-dialog="closeDialog" @confirm-fields="confirmFields" @update-field-data="updateFieldData">
    </my-dialog>

    <!-- 预览表组件 -->
    <my-review-dialog :previewDialogVisible="previewDialogVisible" :preview-data="previewData"
      @close-preview="closePreviewDialog" @confirm-preview="confirmPreview"></my-review-dialog>
  </div>
</template>

<script>
import api from '@/api/api';
import myDialog from './myDialog.vue';
import MyReviewDialog from './myReviewDialog.vue';

export default {
  components: { myDialog, MyReviewDialog },
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
      loading: false,//确认字段弹框的加载状态
      rawData:'',//这个是要给确认字段类型框的原始数据
      // 预览start
      previewDialogVisible: false, // 预览表对话框的显示状态
      previewData: {}, // 预览表数据
      // 预览end
    };
  },
  methods: {
    // 在确认字段类型弹窗中有改变字段类型，传递过来触发该方法
    updateFieldData(sheet, updatedFields) {
      this.fieldConfirmData[sheet] = updatedFields;
    },
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
        console.log('用户取消上传文件');

        // 用户取消选择文件
        this.$message.info('未选择文件，已取消上传');
        this.IsUploadExcelLoading = false; // 关闭上传按钮的加载状态
        return;
      }
      const formData = new FormData();
      formData.append('excel', file);

      try {
        const { tableName } = this;
        const response = await api.uploadConfirm(formData, tableName);
        // console.log('上传字段类型：',response);

        if (response.data.code === 0) {

          this.previewData = JSON.parse(JSON.stringify(response.data.data.Sheet1));
          this.previewDialogVisible = true; // 显示预览表对话框
          // 重点在这里，提取东西
          this.rawData = response.data.data;//给认字段类型框保存原始数据

          
          // this.fieldConfirmDialogVisible = true;  // 弹出字段确认对话框
          // console.log('是没有执行这一句的原因');

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
      async confirmPreview() {
        this.previewDialogVisible = false;
        
        this.fieldConfirmData = Object.fromEntries(
            Object.entries(this.rawData).map(([sheetName, fields]) => [
              sheetName,
              Object.entries(fields).map(([fieldName, fieldType]) => ({
                fieldName,
                fieldType,
              })),
            ])
          );
          this.selectedConfirmSheet = Object.keys(this.fieldConfirmData)[0];
          if (this.uploadType == 'cover') {
            this.fieldConfirmDialogVisible = true;  // 弹出字段确认对话框
          }
          else if (this.uploadType === 'append') {
            // 调用追加上传接口
            await api.uploadAppend(this.tableName);
            // console.log('追加上返回什么：', res2);
            // 成功后刷新整个页面
            window.location.reload();  // 刷新页面
          }
      },
    // 关闭弹窗
    closeDialog() {
      this.fieldConfirmDialogVisible = false;
    },

    // 确认字段类型提交
    async confirmFields() {
      try {
        // console.log('fieldConfirmData',this.fieldConfirmData);
        this.loading = true;
        const fieldListMap = Object.fromEntries(
          Object.entries(this.fieldConfirmData).map(([sheetName, fields]) => [
            sheetName,
            Object.fromEntries(fields.map(field => [field.fieldName, field.fieldType])),
          ])
        );
        // console.log('fieldConfirmData2',this.fieldConfirmData);
        // console.log('fieldListMap',fieldListMap);
        // 调用覆盖上传接口
        if (this.uploadType === 'cover') {

          const { tableName } = this;
          // console.log(tableName);

          // console.log('this.tableName',tableName);

          const res1 = await api.uploadCover(fieldListMap, tableName);

          console.log('res1', res1);

        }
        // else if (this.uploadType === 'append') {
        //   // 调用追加上传接口
        //   const res2 = await api.uploadAppend(fieldListMap, this.tableName);
        //   console.log('res2', res2);

        // }

        this.fieldConfirmDialogVisible = false;
        this.$message.success('所有sheet的字段类型已确认');
        // 成功后刷新整个页面
        window.location.reload();  // 刷新页面
      } catch (error) {
        this.$message('确认字段类型失败');
      } finally {
        this.loading = false; // 操作完成，关闭加载状态
      }
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