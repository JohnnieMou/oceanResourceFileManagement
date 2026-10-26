<template>
    <el-dialog
      title="添加权限"
      :visible="visible"
      width="30%"
      @close="handleClose"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="formData"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="用户名" prop="username" class="form-item">
          <el-select
            v-model="formData.username"
            placeholder="请选择用户名"
            filterable
            clearable
          >
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.username"
              :value="user.username"
            />
          </el-select>
        </el-form-item>
  
        <el-form-item label="表名" prop="tableNames" class="form-item">
          <el-select
            v-model="formData.tableNames"
            multiple
            placeholder="请选择表名"
            collapse-tags
            :max-collapse-tags="3"
          >
            <el-option
              v-for="table in availableTables"
              :key="table.value"
              :label="table.label"
              :value="table.value"
            />
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
      visible: {
        type: Boolean,
        default: false
      },
      editing: {
        type: Boolean,
        default: false
      },
      editData: {
        type: Object,
        default: () => ({})
      }
    },
    data() {
      return {
        formData: {
          username: null, // 用户名
          tableNames: [], // 表名
          voyageStart: null,
          voyageEnd: null,
          yearStart: null,
          yearEnd: null
        },
        rules: {
          username: [
            { required: true, message: '请选择用户名', trigger: 'blur' }
          ],
          tableNames: [
            { required: true, message: '请选择至少一个表名', trigger: 'change' }
          ]
        },
        availableTables: [], // 可用表名列表
        users: [] // 用户列表
      };
    },
    watch: {
      visible(newVal) {
        if (!newVal) {
          this.resetForm();
        }
      },
      editData(newVal) {
        if (newVal && this.editing) {
          this.formData = { ...newVal };
        }
      }
    },
    created() {
      this.fetchUserInfo();
      this.fetchTableList();
    },
    methods: {
      // 获取所有用户信息
      async fetchUserInfo() {
        try {
          const response = await api.fetchUserInfo();
          // console.log('获取用户所有信息的接口：', response);
  
          if (response.data.code === 0) {
            this.users = response.data.data.records; // 假设返回的数据在 records 中
          } else {
            this.$message.error(response.data.msg);
          }
        } catch (error) {
          console.error('获取用户信息失败:', error);
          this.$message.error('获取用户信息失败');
        }
      },
  
      // 获取所有表信息
      async fetchTableList() {
        try {
          const response = await api.listTables();
          // console.log('获取表列表接口：', response);
  
          if (response.data.code === 0 && response.data.data) {
            // 确保 response.data.data 存在
            const tables = response.data.data; // 表列表
            this.availableTables = tables.map(table => ({
              value: table.tableName,
              label: table.tableName
            }));
          } else {
            this.$message.error(response.data.msg || '获取表列表失败');
          }
        } catch (error) {
          console.error('获取表列表失败:', error);
          this.$message.error('获取表列表失败');
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
            // 将用户名转换为用户ID
            const userId = this.users.find(user => user.username === this.formData.username)?.id;
  
            const params = {
              userId: userId, // 使用用户ID
              tableNames: this.formData.tableNames,
              voyageStart: this.formData.voyageStart,
              voyageEnd: this.formData.voyageEnd,
              yearStart: this.formData.yearStart,
              yearEnd: this.formData.yearEnd
            };
  
            if (this.editing) {
              this.$emit('edit-permission', params);
            } else {
              this.$emit('add-permission', params);
            }
  
            this.$message.success(this.editing ? '权限编辑成功' : '权限添加成功');
            this.handleClose();
          } else {
            console.error('表单验证失败');
            return false;
          }
        });
      }
    }
  };
  </script>
  
  <style scoped>
  .el-form{
    display: flex;
    justify-content: flex-start;
    flex-direction: column;
    align-items: start;
  }
  .form-item {
    /* display: flex;
    align-items: center;
    justify-content: flex-start;
    margin-bottom: 20px; */
  }
  
  .el-form-item__label {
    text-align: left;
    flex: 0 0 auto;
    margin-right: 20px;
  }
  
  /* .el-form-item__content {
    flex: 1;
  } */
  </style>