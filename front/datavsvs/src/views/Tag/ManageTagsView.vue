<template>
    <div class="home">
        <!--面包屑导航 -->
        <el-breadcrumb separator-class="el-icon-arrow-right" class="breadCrumb">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '//manageTags' }">管理标签</el-breadcrumb-item>
        </el-breadcrumb>
        <!-- 管理标签 -->
        <div class="formBox">
            <el-form ref="Tags" :model="Tags" label-width="80px" :rules="rules">
                <el-form-item label="创建标签" prop="newTag">
                    <el-input placeholder="请输入新标签名称" v-model="Tags.newTag">
                        <template slot="append">
                            <el-button type="primary" @click="addNewTag" :loading="createLoading" style="color:#44a0ff">添加</el-button>
                        </template>
                    </el-input>
                    
                </el-form-item>

                <el-form-item label="更新标签" prop="editTag">
                    <el-input placeholder="请输入要更新的标签名称" v-model="Tags.editTag">
                        <template slot="append">
                            <el-button type="primary" @click="EditTags" style="color:#44a0ff">更新</el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <!-- 更新修改标签弹出的弹框 -->
                <el-dialog :visible.sync="dialogVisible" title="更新标签">
                    <el-input v-model="newTagName" placeholder="请输入新的标签名称"></el-input>
                    <span slot="footer" class="dialog-footer">
                        <el-button @click="dialogVisible = false">取 消</el-button>
                        <el-button type="primary" @click="updateTag">确 定</el-button>
                    </span>
                </el-dialog>
                <el-form-item label="标签列表">
                    <div v-loading="tagListLoading">
                        <el-tag :key="item.id" v-for="item in TagsValues" closable :disable-transitions="false"
                        size="medium" @close="handleClose(item.tagName)">
                        {{ item.tagName }}
                    </el-tag>
                    </div>
                </el-form-item>
            </el-form>
           
        </div>
    </div>
</template>

<script>
import api from '@/api/api';

export default {
    data() {
        return {
            tagListLoading: false,
            // 更新标签部分
            dialogVisible: false, // 控制对话框的可见性
            newTagName: '', // 存储用户更新时输入的新标签名
            tagIndexToUpdate: null, // 存储需要更新的标签的索引
            // 标签全局
            Tags: {
                newTag: '',
                editTag: ''
            },
            // 获得标签列表存储的地方
            TagsValues: [],
            // 验证规则
            rules: {
                newTag: [
                    { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' }
                ],
                editTag: [
                    { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' }
                ]
            },
            // 控制创建标签的加载中状态
            createLoading: false
        }
    },
    mounted() {
        // console.log('执行1');
        
        this.getTagList(); // 获取标签列表
    },
    methods: {
        // 获取标签列表
        getTagList() {
            // console.log("执行2");
            
            this.tagListLoading = true;
            // console.log(this.tagListLoading,'this.tagListLoading');
            
            try {
                api.getTags()
                    .then((res) => {
                        this.TagsValues = res.data.data;
                    })
                    .catch((error) => {
                        console.error("获取标签列表失败:", error);
                    });

            } catch (error) {
                console.error("在获取标签列表的过程中发生异常:", error);
            } finally {
                this.tagListLoading = false;
            }
        },

        // 创建标签
        addNewTag() {
            const IsTagExists = this.TagsValues.some(item => item.tagName === this.Tags.newTag);

            if (this.Tags.newTag) {
                if (!IsTagExists) {
                    // this.createLoading = true; 

                    // 调用封装后的 API 创建新标签
                    api.createTag(this.Tags.newTag)
                        .then(response => {
                            console.log("创建标签",response);

                            console.log(response.data.code);
                            if (response.data.code === 0) {
                                this.TagsValues.push({ tagName: this.Tags.newTag });
                                this.Tags.newTag = ''; // 清空输入框
                                this.addSucessMesage(); // 显示成功消息
                            } else {
                                this.$message.error('标签创建失败');
                            }
                            this.createLoading = false; 
                        })
                        .catch(error => {
                            console.error('创建标签失败:', error);
                            this.$message.error('请求错误，标签创建失败');
                            this.createLoading = false; 
                        });
                } else {
                    this.$message.error('标签名已存在');
                }
            } else {
                this.$message.error('标签名称不能为空');
            }
        },

        // 编辑标签
        EditTags() {
            const index = this.TagsValues.findIndex(item => item.tagName === this.Tags.editTag);
            if (index !== -1) {
                this.dialogVisible = true;
                this.newTagName = this.Tags.editTag;
                this.tagIndexToUpdate = index;
                console.log('编辑标签了');
            } else {
                this.$message.error('未找到该标签');
            }
        },

        // 更新标签
        updateTag() {
            const IsTagRepeat = this.TagsValues.some(item => item.tagName === this.newTagName);

            if (this.newTagName) {
                if (!IsTagRepeat) {
                    const oldTagName = this.TagsValues[this.tagIndexToUpdate].tagName;
                    // 调用封装后的 API 更新标签
                    api.updateTag(oldTagName, this.newTagName)
                        .then(response => {

                            if (response.data.code === 0) {
                                // 更新本地标签列表
                                this.TagsValues[this.tagIndexToUpdate].tagName = this.newTagName;
                                this.dialogVisible = false; // 关闭弹窗
                                this.Tags.editTag = ''; // 清空输入框
                                this.newTagName = ''; // 清空新的标签输入框
                                this.$message.success('标签已更新');
                            } else {
                                this.$message.error('标签更新失败');
                            }
                        })
                        .catch(error => {
                            console.error('更新标签失败:', error);
                            this.$message.error('请求错误，标签更新失败');
                        });
                } else {
                    this.$message.error('标签名已存在');
                }
            } else {
                this.$message.error('新的标签名称不能为空'); // 提示新的标签名称不能为空
            }
        },

        // 删除标签
        handleClose(tagName) {
            this.$confirm('如删除该标签,带有该标签的表格将失去该标签， 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 调用封装后的 API 删除标签
                api.deleteTag(tagName)
                    .then(response => {
                        console.log('删除成功', response);

                        // 更新标签列表
                        this.TagsValues = this.TagsValues.filter(item => item.tagName !== tagName);
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        });

                    })
                    .catch(error => {
                        console.log(error);
                        this.$message({
                            type: 'error',
                            message: '删除失败，请稍后重试'
                        });
                    });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },

        // 添加标签成功的提示框消息
        addSucessMesage() {
            this.$alert('标签添加成功', '添加标签', {
                confirmButtonText: '确定'
            });
        }
    }
}
</script>
<style lang="scss" scoped>
.home {
    // width: 100%;
    // height: 100%;
    // background-color: pink;
    
    box-sizing: border-box;
    padding: 1.875rem;

    .breadCrumb {
        margin-bottom: 1.25rem
            /* 20/16 */
        ;
    }

    .formBox {
      
        // width: 100%;
        // padding: 1.875rem
        //     /* 30/16 */
        // ;

        .checkTag-Box {
            // background-color: skyblue;
            display: flex;
            flex-wrap: wrap;
            margin-top: 1.25rem
                /* 20/16 */
            ;

            .TagOptions {
                margin-top: 1.25rem
                    /* 20/16 */
                ;
            }
        }
    }

    .el-tag+.el-tag {
        margin-left: 10px;
    }

    .button-new-tag {
        margin-left: 10px;
        height: 32px;
        line-height: 30px;
        padding-top: 0;
        padding-bottom: 0;
    }

    .input-new-tag {
        width: 90px;
        margin-left: 10px;
        vertical-align: bottom;
    }

    // 更新标签弹出的修改提示框
}
</style>