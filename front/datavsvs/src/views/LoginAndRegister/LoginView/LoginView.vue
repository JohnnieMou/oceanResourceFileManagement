<template>
    <div>
        <div class="login-body">
            <div class="login-box">
                <div style="flex: 1; padding: 20px">
                    <div class="form-toggle">
                        <b>账号登录</b>
                    </div>
                    <el-form ref="form" :model="form" size="normal" :rules="rules">
                        <el-form-item prop="username" class="props">
                            <el-input placeholder="账号" clearable v-model="form.username" prefix-icon="el-icon-user" />
                        </el-form-item>
                        <el-form-item prop="password" class="props" style="padding-top:15px">
                            <el-input placeholder="密码" v-model="form.password" show-password
                                prefix-icon="el-icon-lock" />
                        </el-form-item>
                        <el-form-item style="padding-top:15px">
                            <el-button type="primary" style="width: 100%" @click="login" round :loading="isLoading">登
                                录</el-button>
                        </el-form-item>

                        <div style="margin: 10px 0; display: flex;justify-content: space-around;">
                            <span rel="external nofollow" style="color: var(--colorRed)"
                                @click="gotoRegisterView">立即注册</span>
                        </div>
                    </el-form>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import api from '@/api/api';
// import { Message } from 'element-ui';
export default {
    name: "LoginView",
    data() {
        return {
            form: {
                username: '',
                password: '',
            },
            checked: false,
            isLoading: false, // 添加 isLoading 变量
            rules: {
                username: [
                    { required: true, message: '请输入用户名', trigger: 'blur' },
                    { min: 3, max: 10, message: "长度在3到10个字符", trigger: "blur" },
                ],
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                    { min: 6, max: 11, message: '长度在 6 到 11 个字符', trigger: 'blur' }
                ],
            },
        }
    },
    methods: {
        gotoRegisterView() {
            this.$router.push({
                path: '/register',
            })
        },
        login() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    this.isLoading = true;
                    api.login(this.form)
                        .then(res => {
                            // console.log('登录', res);
                            if (res.data.code === 0) {

                                this.$confirm('登录成功', '提示', {
                                    confirmButtonText: '确定',
                                    type: 'success',
                                    cancelButtonText: '',
                                    showCancelButton: false,
                                })
                                localStorage.setItem('token', res.data.data.token);
                                localStorage.setItem('userInfo', JSON.stringify(res.data.data)); // 使用 JSON.stringify 转换对象为字符串
                                this.$router.push({
                                    path: '/'
                                });
                            } else {
                                this.$confirm(res.data.msg, '错误', {
                                    confirmButtonText: '确定',
                                    type: 'error',
                                    cancelButtonText: '',
                                    showCancelButton: false,
                                })
                            }
                        })
                        .catch(err => {
                            console.error('登录失败', err);
                            this.$confirm(err, '错误', {
                                confirmButtonText: '确定',
                                type: 'error',
                                cancelButtonText: '',
                                showCancelButton: false,
                            })
                        })
                        .finally(() => {
                            this.isLoading = false;
                        });
                } else {
                    console.log('验证失败');
                    this.$confirm('验证失败，请检查输入', '警告', {
                        confirmButtonText: '确定',
                        type: 'warning',
                        cancelButtonText: '',
                        showCancelButton: false,
                    })
                    return false;
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