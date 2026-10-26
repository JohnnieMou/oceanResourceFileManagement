<template>
    <el-dialog :title="title" :visible="visible" width="30%" append-to-body>
      <el-form :model="noteForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="noteForm.newTitle"></el-input>
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="noteForm.newContent"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleCancel">取 消</el-button>
        <el-button type="primary" @click="handleConfirm" :loading="loading">确 定</el-button>
      </span>
    </el-dialog>
  </template>
  
  <script>
  import api from '@/api/api';
  
  export default {
    name: 'EditNoteBook',
    props: {
      visible: {
        type: Boolean,
        default: false
      },
      title: {
        type: String,
        default: '修改记事'
      },
      noteId: {
        type: [Number, String],
        default: null
      },
      oldTitle: {
        type: String,
        default: ''
      },
      oldContent: {
        type: String,
        default: ''
      }
    },
    data() {
      return {
        noteForm: {
          id: this.noteId,
          newTitle: this.oldTitle,
          newContent: this.oldContent
        },
        loading: false
      };
    },
    watch: {
      noteId(newVal) {
        if (newVal !== null) {
          this.noteForm.id = newVal;
        }
      },
      oldTitle(newVal) {
        this.noteForm.newTitle = newVal;
      },
      oldContent(newVal) {
        this.noteForm.newContent = newVal;
      }
    },
    mounted() {
      console.log('noteForm:', this.noteForm);
    },
    methods: {
      handleCancel() {
        this.$emit('update:visible', false);
      },
      async handleConfirm() {
        try {
          if (!this.noteForm.newTitle && !this.noteForm.newContent) {
            this.$message.warning('标题和内容不能为空！');
            return;
          }
  
          this.loading = true;
          await this.updateNote();
        } catch (error) {
          this.$message.error(`记事修改失败：${error.message}`);
        } finally {
          this.loading = false;
        }
      },
      async updateNote() {
        try {
          const params = {
            id: this.noteForm.id,
            newTitle: this.noteForm.newTitle,
            newContent: this.noteForm.newContent
          };
  
          const response = await api.updateNoteBook(params);
          console.log('Response:', response);
  
          if (!response || !response.data) {
            throw new Error('接口返回数据格式错误');
          }
  
          if (response.data.code === 0) {
            this.$message.success('记事更新成功！');
            this.$emit('update:visible', false);
            this.$emit('on-success');
          } else {
            this.$message.error(response.data.msg || '记事更新失败');
          }
        } catch (error) {
          console.error('更新记事失败:', error);
          this.$message.error(`记事更新失败：${error.message}`);
        }
      }
    }
  };
  </script>