<template>
    <el-dialog :title="title" :visible="visible" width="30%" append-to-body>
        <el-form :model="noteForm" label-width="80px">
            <el-form-item label="标题">
                <el-input v-model="noteForm.title"></el-input>
            </el-form-item>
            <el-form-item label="内容">
                <el-input type="textarea" v-model="noteForm.content"></el-input>
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
    name: 'AddNoteBook',
    props: {
        visible: {
            type: Boolean,
            default: false
        },
        title: {
            type: String,
            default: '添加记事'
        }
    },
    data() {
        return {
            noteForm: {
                title: '',
                content: ''
            },
            loading: false
        };
    },
    methods: {
        handleCancel() {
            this.$emit('update:visible', false);
            this.resetForm();
        },
        //   添加记事
        async handleConfirm() {
            try {
                // 表单验证
                if (!this.noteForm.title || !this.noteForm.content) {
                    throw new Error('标题和内容不能为空！');
                }

                // 显示加载状态
                this.loading = true;

                // 发送 POST 请求
                const params = {
                    title: this.noteForm.title,
                    content: this.noteForm.content
                };
                try {
                    const response = await api.addNoteBook(params);
                    if (response.data.code === 0) {
                        this.$message.success('记事添加成功！');
                        this.$emit('update:visible', false);
                        this.$emit('on-success');
                    } else {
                        this.$message.error(response.data.msg);
                    }
                } catch (error) {
                    console.error('添加记事失败:', error);
                    this.$message.error('添加记事失败');
                }

               
            } catch (error) {
                // 处理错误
                this.$message.error(`记事添加失败：${error.message}`);
            } finally {
                // 隐藏加载状态
                this.loading = false;
            }
        },
        
        resetForm() {
            this.noteForm = {
                title: '',
                content: ''
            };
        }
    }
};
</script>