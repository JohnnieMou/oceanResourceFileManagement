<template>
  <el-dialog title="修改权限" :visible="visible" width="30%" @close="handleClose">
    <el-form :model="formData" :rules="rules" ref="formData" label-width="120px" class="demo-ruleForm">
      <el-form-item label="表名" prop="tableNames" class="form-item">
        <!-- 表名改为普通展示，不可编辑 -->
        <el-input v-model="formData.tableNames" readonly></el-input>
      </el-form-item>

      <el-divider content-position="left">航次范围</el-divider>
      <el-form-item label="起始航次" prop="voyageStart" class="form-item">
        <el-input v-model.number="formData.voyageStart" placeholder="请输入起始航次（可选）"></el-input>
      </el-form-item>

      <el-form-item label="结束航次" prop="voyageEnd" class="form-item">
        <el-input v-model.number="formData.voyageEnd" placeholder="请输入结束航次（可选）"></el-input>
      </el-form-item>

      <el-divider content-position="left">年份范围</el-divider>
      <el-form-item label="起始年份" prop="yearStart" class="form-item">
        <el-input v-model.number="formData.yearStart" placeholder="请输入起始年份（可选）"></el-input>
      </el-form-item>

      <el-form-item label="结束年份" prop="yearEnd" class="form-item">
        <el-input v-model.number="formData.yearEnd" placeholder="请输入结束年份（可选）"></el-input>
      </el-form-item>

      <el-form-item class="form-item">
        <el-button type="primary" @click="submitForm('formData')">提交</el-button>
        <el-button @click="handleClose">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
import api from '@/api/api';

export default {
  name: 'EditPermissionDialog',
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    userInfo: {
      type: Object,
      default: () => ({}),
    },
    currentRow: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      formData: {
        userId: this.userInfo.id, // 用户ID
        tableNames: '', // 表名
        voyageStart: null,
        voyageEnd: null,
        yearStart: null,
        yearEnd: null,
      },
      rules: {
        tableNames: [
          { required: true, message: '请选择至少一个表名', trigger: 'change' },
        ],
      },
    };
  },
  watch: {
    visible(newVal) {
      if (!newVal) {
        this.resetForm();
      }
    },
    userInfo(newVal) {
      if (newVal) {
        this.formData.userId = newVal.id; // 更新用户ID
      }
    },
    currentRow(newVal) {
      if (newVal) {
        this.formData.tableNames = newVal.tableCnName;
        this.formData.voyageStart = newVal.voyageStart;
        this.formData.voyageEnd = newVal.voyageEnd;
        this.formData.yearStart = newVal.yearStart;
        this.formData.yearEnd = newVal.yearEnd;
      }
    },
  },
  methods: {
    resetForm() {
      if (this.$refs.formData) {
        this.$refs.formData.resetFields();
      }
    },
    handleClose() {
      this.$emit('update:visible', false);
    },
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          const params = {
            userId: this.formData.userId, // 使用用户ID
            tableNames: [this.formData.tableNames], // 修改为数组格式
            voyageStart: this.formData.voyageStart,
            voyageEnd: this.formData.voyageEnd,
            yearStart: this.formData.yearStart,
            yearEnd: this.formData.yearEnd,
          };
          try {
            const response = await api.updatePermission(params);
            if (response.data.code === 0) {
              this.$message.success('权限修改成功');
              this.$emit('update-permission');
              this.handleClose();
            } else {
              this.$message.error(response.data.msg);
            }
          } catch (error) {
            console.error('修改权限失败:', error);
            this.$message.error('修改权限失败');
          }
        } else {
          console.error('表单验证失败');
          return false;
        }
      });
    },
  },
};
</script>

<style scoped>
.el-form {
  display: flex;
  justify-content: flex-start;
  flex-direction: column;
  align-items: start;
}

.el-form-item__label {
  text-align: left;
  flex: 0 0 auto;
  margin-right: 20px;
}
</style>