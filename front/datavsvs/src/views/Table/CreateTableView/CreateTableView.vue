<template>
    <div class="home">
        <!--面包屑导航 -->
        <el-breadcrumb separator-class="el-icon-arrow-right" class="breadCrumb">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>创建表</el-breadcrumb-item>
        </el-breadcrumb>
        <!-- 创建表 -->
        <div class="tableBox">
            <el-form ref="table" :model="table" label-width="80px" :rules="rules">
                <el-form-item label="表名" prop="name">
                    <el-input v-model="table.name" placeholder="请输入表名"></el-input>
                </el-form-item>
                <el-form-item label="摘要">
                    <el-input type="textarea" :rows="2" placeholder="请输入摘要" v-model="table.textarea"
                        :autosize="{ minRows: 6 }">
                    </el-input>
                </el-form-item>
                <el-form-item label="标签">
                    <div v-loading="tagListLoading">
                        <el-input placeholder="请输入新标签" v-model="Tags.newTag">
                            <template slot="prepend">
                                <div @click="addNewTag"> 添加</div>
                            </template>
                        </el-input>
                        <el-checkbox-group v-model="table.tags" class="checkTag-Box">
                            <el-checkbox :label="item.tagName" border v-for="item in TagsValues" :key="item.id"
                                class="TagOptions"></el-checkbox>
                        </el-checkbox-group>
                    </div>
                </el-form-item>
                <el-form-item>
                    <!-- <el-button type="primary" @click="onSubmit" :disabled="!table.name">立即创建</el-button> 
                     -->
                    <el-button type="primary" @click="onSubmit" :disabled="!table.name" :loading="isLoading">
                        立即创建
                    </el-button>
                    <!-- <el-button>取消</el-button> -->
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
            currentTableID: '',//创建表格后要进入的表格详情页ID
            tagListLoading: false,
            // 表的相关属性
            table: {
                name: '',
                textarea: '',
                tags: [],
                newTag: ''
            },
            TablesValues: [],
            // 加载状态
            isLoading: false,
            // 表单验证规则
            rules: {
                name: [
                    { required: true, message: '请输入表名', trigger: 'blur' },
                    { min: 1, max: 30, message: '长度在 1 到 30 个字符', trigger: 'blur' }
                ],
            },
            // 选择和新增标签相关
            TagsValues: [],
            Tags: {
                newTag: '',
                editTag: ''
            }
        }
    },
    mounted() {
        this.getTagList();  // 获取标签列表
        this.getTableList(); // 获取表列表
    },
    methods: {
        // 获取表列表
        getTableList() {
            api.listTables()  // 使用二次封装接口调用
                .then(res => {
                    this.TablesValues = res.data.data;
                })
                .catch(error => {
                    console.error('获取表列表失败:', error);
                });
        },
        // 获取标签列表
        getTagList() {
            this.tagListLoading = true;
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

        // 创建表时想添加新标签
        addNewTag() {
            const IsTagExists = this.TagsValues.some(item => item.tagName === this.Tags.newTag);
            if (this.Tags.newTag) {
                if (!IsTagExists) {
                    // 调用二次封装的接口创建新标签
                    api.createTag(this.Tags.newTag)
                        .then(response => {
                            console.log('标签的', response);

                            if (response.data.code === 0) {
                                console.log(response.data.code);

                                const newTag = {
                                    tagName: this.Tags.newTag
                                };
                                // 将新标签推入 TagsValues 数组
                                // this.TagsValues.push(newTag.tagName);
                                this.TagsValues.push({ tagName: this.Tags.newTag })

                                this.getTagList();  // 更新标签列表
                                // 清空输入框
                                this.Tags.newTag = '';
                                // 默认选中
                                this.table.tags.push(newTag.tagName);

                                this.addSucessMesage();  // 显示成功消息
                            } else {
                                // this.$message.error('标签创建失败');
                            }
                        })
                        .catch(error => {
                            console.error('创建标签失败:', error);
                            // this.$message.error('请求错误，标签创建失败');
                        });
                } else {
                    this.$message.error('标签名已存在');
                }
            } else {
                this.$message.error('标签名称不能为空');
            }
        },

        // 添加标签成功的提示框消息
        addSucessMesage() {
            this.$alert('您创建了一个新的标签', '新增标签', {
                confirmButtonText: '确定'
            });
        },

        // 检查表名是否存在
        checkTableNameExists() {
            return this.TablesValues.some(item => item.tableName === this.table.name);
        },

        // 创建表
        // 创建表
        onSubmit() {

            // 检查表名是否已存在
            if (this.checkTableNameExists()) {
                this.$confirm(`表名 "${this.table.name}" 已存在，请使用其他表名。`, '提示', {
                    confirmButtonText: '确定',
                    type: 'warning',
                    showCancelButton: false // 不显示取消按钮
                })
                return;
            }

            // 设置加载状态为 true 并禁用按钮
            this.isLoading = true;

            // 构建请求参数
            const requestData = {
                tableName: this.table.name,
                description: this.table.textarea, // 可选参数，传递表格描述
                tags: this.table.tags  // 可选参数
            };

            // 调用二次封装接口创建表格
            api.createTable(requestData)
                .then(response => {
                    if (response.data.code === 0) {
                        this.$confirm('表格创建成功', '提示', {
                            confirmButtonText: '确定',
                            type: 'success',
                            cancelButtonText: '',
                            showCancelButton: false,
                        }).then(() => {
                            // 跳转进入详情页
                            this.IntoDetailTable();
                        })
                    }
                })
                .catch(error => {
                    console.error('创建表格失败:', error);
                    this.$message.error('创建失败，请稍后重试');
                })
                .finally(() => {
                    // 不管成功或失败，重置加载状态并启用按钮
                    this.isLoading = false;
                });
        },
        // 跳转进入详情页
        async IntoDetailTable() {
            try {
                const res = await api.listTables();

                if (res.data.code === 0) {
                    this.currentTableID = res.data.data.length - 1;
                    this.$router.push({
                        path: `/table/${this.currentTableID}`,
                        query: {
                            tableName: encodeURIComponent(this.table.name),
                            description: encodeURIComponent(this.table.textarea),
                            tags: encodeURIComponent(JSON.stringify(this.table.tags))
                        },
                    });
                    this.resetForm();  // 重置表单
                }

            } catch (err) {
                console.error('获取表格数据失败', err);
            }
        },
        // 重置表单
        resetForm() {
            this.table.name = '';
            this.table.textarea = '';
            this.table.tags = [];
            this.Tags.newTag = '';
        },
    }
}
</script>


<style lang="scss" scoped>
.home {
    box-sizing: border-box;
    padding: 1.875rem;

    .breadCrumb {
        margin-bottom: 1.25rem
            /* 20/16 */
        ;
    }

    .tableBox {
        // width: 56.25rem
        //     /* 900/16 */
        // ;
        // width: 100%;
        // // height: 100%;
        // // background-color: pink;
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
}
</style>