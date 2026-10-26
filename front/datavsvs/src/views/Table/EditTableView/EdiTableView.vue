<template>
    <div class="home">
        <!--面包屑导航 -->
        <el-breadcrumb separator-class="el-icon-arrow-right" class="breadCrumb">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/home' }">选择表</el-breadcrumb-item>
            <el-breadcrumb-item>编辑表</el-breadcrumb-item>
        </el-breadcrumb>
        <!-- 编辑页面 -->
        <div class="tableBox">
            <el-form ref="table" :model="table" label-width="80px" :rules="rules">
                <el-form-item label="表名" prop="name">
                    <el-input v-model="table.name" placeholder="请输入表名"></el-input>
                </el-form-item>
                <el-form-item label="摘要">
                    <el-input type="textarea" :rows="2" placeholder="请输入摘要" v-model="table.textarea"
                        :autosize="{ minRows: 6 }"></el-input>
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
                    <el-button type="primary" @click="onSubmit" :disabled="!table.name"
                        :loading="isLoading">保存修改</el-button>
                    <el-button @click="cancelEdit">取消</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script>
import api from "@/api/api"; // 假设你的二次封装接口文件名为 api.js

export default {
    data() {
        return {
            tagListLoading: false,
            table: {
                name: "",
                textarea: "",
                tags: [],
                newTag: "",
            },
            TablesValues: [],
            // 加载状态
            isLoading: false,
            rules: {
                name: [
                    { required: true, message: "请输入表名", trigger: "blur" },
                    { min: 1, max: 30, message: "长度在 1 到 30 个字符", trigger: "blur" },
                ],
            },
            TagsValues: [],
            Tags: {
                newTag: '',
                editTag: ''
            },

            // 获取上一个页面的路由路径start
            detailTableId: '',
            // 获取上一个页面的路由路径end
        };
    },
    mounted() {
        // 获取上一个页面的路由路径start
        this.getDetailTableId();
        // 获取上一个页面的路由路径end

        this.getTagList(); // 获取标签列表
        this.getTableList(); // 获取表列表
        const tableName = decodeURIComponent(this.$route.params.tableName); // 从路由中获取表格名称,解码并赋值
        // console.log("tableName", tableName);
        if (tableName) {
            this.getTableInfo(tableName); // 获取表格信息
        }
    },
    methods: {
        // 获取上一个页面的路由路径start
        getDetailTableId() {
            this.detailTableId = this.$store.state.detailTableId
            console.log("编辑页面的上一个页面的路由路径:detailTableId:", this.detailTableId);
        },
        // 获取上一个页面的路由路径end
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

        // 获取表格数据，用于编辑
        getTableInfo(tableName) {
            api
                .getTableInfo(tableName)
                .then((response) => {
                    // console.log("获取表格信息：",response);

                    const data = response.data.data;
                    this.table.name = data.tableName;
                    this.table.textarea = data.description || ""; // 可选项
                    this.table.tags = data.tags || []; // 填充标签数据
                })
                .catch((error) => {
                    console.error("获取表格信息失败:", error);
                });
        },

        // 修改表时想添加新标签
        addNewTag() {
            const IsTagExists = this.TagsValues.some(item => item.tagName === this.Tags.newTag);
            console.log('this.Tags.newTag', this.Tags.newTag);

            if (this.Tags.newTag) {
                // console.log(' this.Tags.newTag1', this.Tags.newTag);
                if (!IsTagExists) {
                    // 调用二次封装的接口创建新标签
                    // console.log(' this.Tags.newTag', this.Tags.newTag);

                    api.createTag(this.Tags.newTag)
                        .then(response => {
                            if (response.data.code === 0) {
                                const newTag = {
                                    tagName: this.Tags.newTag
                                };
                                // 将新标签推入 TagsValues 数组
                                this.TagsValues.push(newTag.tagName);
                                this.getTagList();  // 更新标签列表
                                // 清空输入框
                                this.Tags.newTag = '';
                                // 默认选中
                                this.table.tags.push(newTag.tagName);

                                this.addSucessMesage();  // 显示成功消息
                            } else {
                                this.$message.error('标签创建失败');
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
            this.$alert("您创建了一个新的标签", "新增标签", {
                confirmButtonText: "确定",
            });
        },
        // 检查表名是否存在
        checkTableNameExists() {
            return this.TablesValues.some(item => item.tableName === this.table.name);
        },
        // 保存修改
        // 保存修改
        onSubmit() {
            // 获取旧表名
            const oldTableName = this.$route.params.tableName;

            // 检查表名是否已更改
            if (this.table.name !== oldTableName) {
                // 如果表名已更改，检查新表名是否已存在
                if (this.checkTableNameExists()) {
                    this.$confirm(`表名 "${this.table.name}" 已存在，请使用其他表名。`, '提示', {
                        confirmButtonText: '确定',
                        type: 'warning',
                        showCancelButton: false // 不显示取消按钮
                    });
                    return;
                }
            }
            // 判断是否有更改表名
            // if()
            let requestData = {};
            if (this.table.name === oldTableName) {
                requestData = {
                    description: this.table.textarea, // 表格描述
                    newTableName: "", // 新表格名称
                    oldTableName: oldTableName, // 旧表格名称（从路由中获取）
                    tags: this.table.tags // 选中的标签数组
                };
            } else {
                // 构建请求数据
                requestData = {
                    description: this.table.textarea, // 表格描述
                    newTableName: this.table.name, // 新表格名称
                    oldTableName: oldTableName, // 旧表格名称（从路由中获取）
                    tags: this.table.tags // 选中的标签数组
                };

            }

            // 调用封装的修改表格接口
            api.updateTable(requestData)
                .then((response) => {
                    if (response.data.code === 0) {
                        this.$message.success("表格修改成功");

                         // 修改成功后跳转到表格详情页面
                        this.$router.push({
                            path: `/table/${this.detailTableId}`,
                            query: {
                                tableName: encodeURIComponent(this.table.name),
                                description: encodeURIComponent(this.table.textarea),
                                tags: encodeURIComponent(JSON.stringify(this.table.tags))
                            },
                        });
                    }

                })
                .catch((error) => {
                    console.error("修改表格失败:", error);
                    this.$message.error("修改失败，请稍后重试");
                });
        },

        // 取消编辑
        cancelEdit() {
            this.$router.push("/"); // 取消编辑跳转回表格列表页面
        },
    },
};
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
        // width: 56.25rem;
        // width: 100%;
        // padding: 1.875rem;
    }

    .checkTag-Box {
        display: flex;
        flex-wrap: wrap;
        margin-top: 1.25rem;
    }

    .TagOptions {
        margin-top: 1.25rem;
    }
}
</style>