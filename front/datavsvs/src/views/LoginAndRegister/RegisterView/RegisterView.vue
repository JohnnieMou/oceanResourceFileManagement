<template>
    <div>
        <div class="login-body">
            <div class="login-box">
                <div style="flex: 1; padding: 20px">
                    <div class="form-toggle">
                        <b>用户注册</b>
                    </div>
                    <el-form ref="registerForm" :model="registerForm" size="normal" :rules="rules">
                        <!-- <el-form-item prop="name" class="props" style="margin-bottom: 1rem /* 16/16 */">
                            <el-input placeholder="姓名" clearable v-model="registerForm.name" prefix-icon="el-icon-user" />
                        </el-form-item> -->
                        <el-form-item prop="username" class="props">
                            <el-input placeholder="账号" clearable v-model="registerForm.username"
                                prefix-icon="el-icon-user" />
                        </el-form-item>
                        <el-form-item prop="password" class="props" style="padding-top:15px">
                            <el-input placeholder="密码" v-model="registerForm.password" show-password
                                prefix-icon="el-icon-lock" />
                        </el-form-item>
                        <el-form-item prop="confirmPassword" class="props" style="padding-top:15px">
                            <el-input placeholder="确认密码" v-model="registerForm.confirmPassword" show-password
                                prefix-icon="el-icon-lock" />
                        </el-form-item>
                        <el-form-item style="padding-top:15px">
                            <el-button type="primary" style="width: 100%" @click="register" round>注 册</el-button>
                        </el-form-item>

                        <div style="margin: 10px 0; display: flex;justify-content: space-around;">
                            <span rel="external nofollow" style="color: var(--colorRed)"
                                @click="gotoLoginView">已有账号？登录</span>
                        </div>
                    </el-form>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import api from '@/api/api';
export default {
    name: "RegisterView",
    data() {
        return {
            registerForm: {
                // name: '',
                username: '',
                password: '',
                confirmPassword: '',
            },
            isLoading: false,
            rules: {
                // name: [
                //     { required: true, message: '请输入姓名', trigger: 'blur' },
                //     { min: 2, max: 10, message: "长度在2到10个字符", trigger: "blur" },
                // ],
                username: [
                    { required: true, message: '请输入用户名', trigger: 'blur' },
                    { min: 3, max: 20, message: "长度在3到20个字符", trigger: "blur" },
                ],
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                    { min: 6, max: 11, message: '长度在 6 到 11 个字符', trigger: 'blur' }
                ],
                confirmPassword: [
                    { required: true, message: '请确认密码', trigger: 'blur' },
                    {
                        validator: (rule, value, callback) => {
                            if (value !== this.registerForm.password) {
                                callback(new Error('两次输入密码不一致'));
                            } else {
                                callback();
                            }
                        }, trigger: 'blur'
                    }
                ],
            },
        }
    },
    methods: {
        gotoLoginView() {
            this.$router.push({
                path: '/login',
            })
        },
        register() {
            this.$refs.registerForm.validate((valid) => {
                if (valid) {
                    this.isLoading = true;
                    const sendData = {
                        // name: this.registerForm.name,
                        username: this.registerForm.username,
                        password: this.registerForm.password
                    };
                    api.register(sendData)
                        .then(res => {
                            if (res.data.code === 0) {
                                this.$confirm('注册成功', '提示', {
                                    confirmButtonText: '确定',
                                    type: 'success',
                                    cancelButtonText: '', // 将取消按钮文本设置为空字符串
                                    showCancelButton: false, // 明确设置不显示取消按钮
                                }).then(() => {
                                    // 用户点击确认按钮后的操作
                                    this.$router.push({
                                        path: '/login',
                                    });
                                }).catch(() => {
                                    // 用户点击取消按钮或关闭消息框的操作，这里可以不写，因为只有一个确定按钮
                                });
                            }else{
                                this.$confirm('注册失败', '错误', {
                                    confirmButtonText: '确定',
                                    type: 'error',
                                    cancelButtonText: '', // 将取消按钮文本设置为空字符串
                                    showCancelButton: false, // 明确设置不显示取消按钮
                                }).then(() => {
                                    this.registerForm.username='';
                                })
                            }

                        })
                        .catch(err => {
                            console.error('注册失败', err);
                            this.$message.error('注册失败，请检查用户名和密码');
                            //  this.$message.error('注册失败，请检查用户名和密码', '错误', {
                            //     type: 'error'
                            // });
                        })
                        .finally(() => {
                            this.isLoading = false;
                        });
                } else {
                    this.$message.warning('验证失败，请检查输入');
                }
            });
        }
    }
}
</script>
<style scoped lang="scss">
.login-body {
    position: relative;
    height: 50rem
        /* 800/16 */
    ;

    .login-box {
        width: 300px;
        border-radius: 10px;
        box-shadow: 0 0 10px -2px cornflowerblue;
        display: flex;

        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
    }
}


.form-toggle {
    margin: 20px 0;
    text-align: center
}

.form-toggle b {
    font-size: 20px;
    cursor: pointer;
}

/*  */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.ml-5 {
    margin-left: 5px;
}

.ml-10 {
    margin-left: 10px;
}

.mr-5 {
    margin-right: 5px;
}

.pd-10 {
    padding: 10px;
}

body {
    margin: 0;
    padding: 0;
    font-size: 14px;
    color: #666;
    --colorRed: orangered;
}

a {
    text-decoration: none;
}
</style>