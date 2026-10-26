<template>
  <el-dialog title="添加权限" :visible="visible" width="30%" @close="handleClose">
    <el-form :model="formData" :rules="rules" ref="formData" label-width="120px" class="demo-ruleForm">
      <el-form-item label="未授权表名" prop="tableNames" class="form-item">
        <el-select v-model="formData.tableNames" multiple placeholder="请选择表名" collapse-tags :max-collapse-tags="3">
          <el-option v-for="table in availableTables" :key="table.value" :label="table.label" :value="table.value" />
        </el-select>
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
  name: 'AddPermissionDialog',
  props: {
    userInfo: {
      type: Object,
      default: () => ({}),
    },
    visible: {
      type: Boolean,
      default: false,
    }
  },
  data() {
    return {
      formData: {
        userId: this.userInfo.id, // 用户ID
        tableNames: [], // 表名
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
      availableTables: [], // 可用表名列表（未授权的表）
    };
  },
  watch: {
    visible(newVal) {
      if (!newVal) {
        this.resetForm();
      }
      this.fetchUserNoPermissionTableList();
    },
    userInfo(newVal) {
      if (newVal) {
        this.formData.userId = newVal.id; // 更新用户ID
        // console.log('newVal.id：',newVal.id);
        this.fetchUserNoPermissionTableList();
      }
    },
  },
  mounted() {
  },
  methods: {
      
    // 获取用户未取得授权的表列表
    async fetchUserNoPermissionTableList() {
      try {
        // console.log('this.formData.userId:', this.formData.userId);

        const response = await api.fetchListNoPermissionTables(this.formData.userId);
        // console.log('获取未授权表列表接口：', response);

        if (response.data.code === 0 && response.data.data) {
          // 确保 response.data.data 存在
          const tables = response.data.data; 
          this.availableTables = tables.map(table => ({
            value: table, 
            label: table, 
          }));
        } else {
          this.$message.error(response.data.msg || '获取未授权表列表失败');
        }
      } catch (error) {
        console.error('获取未授权表列表失败:', error);
        this.$message.error('获取未授权表列表失败');
      }
    },

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
            tableNames: this.formData.tableNames,
            voyageStart: this.formData.voyageStart,
            voyageEnd: this.formData.voyageEnd,
            yearStart: this.formData.yearStart,
            yearEnd: this.formData.yearEnd,
          };
          this.$emit('add-permission', params);
         

          this.$message.success('权限添加成功');
          this.handleClose();
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