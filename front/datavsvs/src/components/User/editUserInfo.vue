<template>
    <el-dialog title="编辑用户信息" :visible="visible" width="30%" @close="handleClose">
        <el-form :model="formData" :rules="rules" ref="editForm" label-width="120px" class="demo-ruleForm">
            <!-- <el-form-item label="用户ID" prop="id" class="form-item">
                <el-input v-model="formData.id" placeholder="用户ID" disabled></el-input>
            </el-form-item> -->

            <el-form-item label="姓名" prop="name" class="form-item">
                <el-input v-model="formData.name" placeholder="请输入姓名"></el-input>
            </el-form-item>

            <el-form-item label="用户名" prop="username" class="form-item">
                <el-input v-model="formData.username" placeholder="请输入用户名"></el-input>
            </el-form-item>

            <el-form-item label="角色" prop="roleCode" class="form-item">
                <el-select v-model="formData.roleCode" placeholder="请选择角色" :disabled="!isAdmin">
                    <el-option label="管理员" :value="1"></el-option>
                    <el-option label="普通用户" :value="2"></el-option>
                    <el-option label="游客" :value="3"></el-option>
                </el-select>
            </el-form-item>

            <el-form-item class="form-item">
                <el-button type="primary" @click="submitForm('editForm')">提交</el-button>
                <el-button @click="handleClose">取消</el-button>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>

<script>
//   import api from '@/api/api';

export default {
    name: 'EditUserInfo',
    props: {
        visible: {
            type: Boolean,
            default: false,
        },
        userInfo: {
            type: Object,
            default: () => ({}),
        },
    },
    data() {
        return {
            formData: {
                id: '', // 用户ID
                name: '', // 姓名
                username: '', // 用户名
                roleCode: 2, // 默认角色为普通用户
            },
            rules: {
                id: [{ required: true, message: '用户ID不能为空', trigger: 'blur' }],
                name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
                username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
                roleCode: [{ required: true, message: '角色不能为空', trigger: 'change' }],
            },
            isAdmin: false, // 是否是管理员
        };
    },
    watch: {
        visible(newVal) {
            if (newVal) {
                this.initFormData();
            }
        },
        userInfo(newVal) {
            if (newVal) {
                this.formData = { ...newVal };
            }
        },
    },
    mounted() {
        this.fetchUserInfo();
    },
    methods: {
        // 获取用户信息，现在是从token拿到，接口修改后用接口拿，星星
        fetchUserInfo() {
            const currentUser = localStorage.getItem('userInfo');

            if (currentUser) {
                try {
                    // 将字符串解析为对象
                    const parsedUser = JSON.parse(currentUser);
                    // console.log('currentUser:', parsedUser);

                    // 判断是否是管理员
                    this.isAdmin = parsedUser.roleCode === 1;
                } catch (error) {
                    console.error('解析用户信息失败:', error);
                }
            } else {
                console.log('未找到用户信息');
            }
        },
        initFormData() {
            if (this.userInfo) {
                this.formData = { ...this.userInfo };
            }
        },
        resetForm() {
            if (this.$refs.editForm) {
                this.$refs.editForm.resetFields();
            }
        },
        handleClose() {
            this.$emit('update:visible', false);
        },
        submitForm(formName) {
            this.$refs[formName].validate(async (valid) => {
                if (valid) {
                    this.$emit('update-user', this.formData);
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

.form-item {
    margin-bottom: 20px;
}

.el-form-item__label {
    text-align: left;
    flex: 0 0 auto;
    margin-right: 20px;
}
</style>