<template>
    <div>
        <!-- 表格信息的折叠面板 -->
        <el-collapse v-model="BigActiveNames">
            <el-collapse-item name="1">
                <template slot="title">
                    <h2>{{ tableName }}</h2>
                    <i class="header-icon el-icon-info"></i>
                    <!-- 标签的展示 -->
                    <el-tag class="tags" v-for="(tag, index) in tags" :key="index">
                        {{ tag }}
                    </el-tag>
                </template>
                <div>
                    <!-- 摘要的折叠 -->
                    <el-collapse v-model="SmallActiveNames">
                        <el-collapse-item name="1">
                            <template slot="title">
                                <div class="mainContent">摘要</div>
                            </template>
                            <div>{{ description }}</div>
                        </el-collapse-item>
                    </el-collapse>
                    <div class="OperationBox">
                        <!-- 文件操作 -->
                        <div class="ExcelOperationBox">
                            <!-- 上传Excel文件按钮 -->
                            <div class="UploadfileBox">
                                <my-upload-exl :hasExistingData="this.hasExistingData" :tableName=tableName>
                                </my-upload-exl>
                            </div>
                            <!-- 下载Excel文件按钮 -->
                            <div class="DownloadfileBox">
                                <my-download-excel :table-name="tableName" :sheet-name="'Sheet1'"></my-download-excel>
                            </div>
                        </div>
                        <!--表格操作：选择表格选择器 字段管理 修改信息 条件查询 -->
                        <!-- <div class="TableOperationBox">
                            选择Sheet选择器
                            <el-select v-model="value" placeholder="请选择">
                                <el-option v-for="item in SheetOptions" :key="item.value" :label="item.label"
                                    :value="item.value">
                                </el-option>
                            </el-select>

                        </div> -->
                        <div class="TableOperationBox">
                            <!-- 字段管理 -->
                            <el-button type="primary" @click="openFieldManageDialog">字段管理</el-button>
                            <my-field-manage :visible.sync="FieldManageDialogVisible" :table-name="this.tableName"
                                :sheet-name="this.sheetName" ref="myFieldManage"/>
                            <!-- 修改信息 -->
                            <el-button type="primary" class="EditInformation" @click="editTableMessage">修改信息</el-button>

                            <!-- 条件查询 -->
                            <el-button @click="dialog = true" type="primary" class="ConditionalQuery">条件查询</el-button>
                        </div>
                    </div>

                </div>
            </el-collapse-item>
            <el-collapse-item name="2">
                <template slot="title">
                    <div class="mainContent">统计信息</div>
                </template>
                <!-- number数据类型的统计展示 -->
                <div>
                    <my-statistics-table :tableName="tableName" :sheetName="sheetName"></my-statistics-table>
                </div>
            </el-collapse-item>


        </el-collapse>
        <!-- 条件查询弹窗 -->
        <div class="my-demo-drawer">
            <el-drawer title="条件查询" :before-close="handleClose" :visible.sync="dialog" direction="ltr"
                custom-class="demo-drawer" ref="drawer">
                <div class="demo-drawer__content">
                    <my-conditional-query :tableName="tableName" :sheetName="sheetName"
                        ref="myConditionalQuery"></my-conditional-query>
                    <div class="conditional-query-button-group">
                        <el-button @click="cancelForm" class="demo-drawer__footer">取 消</el-button>
                        <div class="demo-drawer__footer">
                            <el-button type="primary" :loading="loading" @click="handleButtonClick">{{
                                loading
                                    ?
                                    '查询中 ...' : '确定查询' }}</el-button>
                        </div>
                    </div>
                </div>
            </el-drawer>
        </div>
        <!-- 字段管理弹窗,好像是没用的？我啥时候写的这里？？？？留个底 -->
        <!-- <my-field-manage :uploadType="uploadType" :fieldManageVisible="fieldManageVisible"
            :selectedConfirmSheet="selectedConfirmSheet" :fieldConfirmData="fieldConfirmData"
            @close-dialog="closeDialog" @confirm-fields="confirmFields"></my-field-manage> -->
    </div>
</template>


<script>
import api from '@/api/api';
import { mapState } from 'vuex';
import myConditionalQuery from './myConditionalQuery.vue';
import MyUploadExl from './myUploadExl.vue';
import MyFieldManage from './myFieldManage.vue';
import MyDownloadExcel from './myDownloadExcel.vue';
import MyStatisticsTable from './myStatisticsTable.vue';
export default {
    components: {
        myConditionalQuery,
         MyUploadExl, 
         MyFieldManage, 
         MyDownloadExcel,
        MyStatisticsTable
    },
    props: {
        tableName: {
            type: String,
            required: true,
            default: 'testTable'
        },
        description: {
            type: String,
            required: true,
            default: '暂无摘要'
        },
        tags: {
            type: Array,
            required: false
        },
        currentPage: {
            type: Number,
            required: true
        },
        pageSize: {
            type: Number,
            required: true
        },
        ConditionQueryResultCurrentPage: {
            type: Number,
            required: true
        },
        ConditionQueryResultpageSize: {
            type: Number,
            required: true
        }
    },
    watch: {
        // 监听 ConditionQueryResultCurrentPage 的变化
        ConditionQueryResultCurrentPage(newVal, oldVal) {
            console.log(`ConditionQueryResultCurrentPage changed from ${oldVal} to ${newVal}`);
            this.submitQuery();
            // this.handlePageChange(newVal);
        },
        // 监听 ConditionQueryResultpageSize 的变化
        ConditionQueryResultpageSize(newVal, oldVal) {
            console.log(`ConditionQueryResultpageSize changed from ${oldVal} to ${newVal}`);
            this.submitQuery();
            // this.handlePageSizeChange(newVal);
        }
    },
    computed: {
        ...mapState([
            'queryConditions'
        ])
    },
    data() {
        return {
            // 当前sheetName,是传递给条件查询组件渲染字段的
            sheetName: "Sheet1",
            // myUpload组件
            hasExistingData: false,//是否已经有Sheet,无，则追加上传为不可选
            // 查询条件的抽屉的data
            dialog: false,
            loading: false,

            // 折叠面板的data
            BigActiveNames: ['1', '2'],//大折叠面板
            // SmallActiveNames: ['1','2'],
            SmallActiveNames: ['1'],//摘要折叠面板

            // Sheet选择器的data
            SheetOptions: [ // 下拉选项数据
                {
                    value: 'sheet1',
                    label: 'sheet1'
                },
                {
                    value: 'sheet2',
                    label: 'sheet2'
                }
            ],
            value: '',

            // 字段管理组件
            FieldManageDialogVisible: false,


            // 用于存储列数据
            SheetcolumnsData: [],
            // uploadType: "",
            // fieldManageVisible: "",
            // selectedConfirmSheet: "",
            // fieldConfirmData: "",
            // closeDialog: "",
            // confirmFields: ""

        };
    },
    methods: {
        //Sheet选择器的data数据获取 
        async fetchSheetList() {
            try {
                const response = await api.getSheets(this.tableName);
                // console.log('res2333333', response);
                if (response.data.code === 200) {
                    const options = response.data.data.map(sheet => ({
                        value: sheet,
                        label: sheet
                    }));
                    this.SheetOptions = options;
                }

            } catch (error) {
                console.error("获取Sheet列表失败", error);

            }
        },
        // 修改表格信息
        editTableMessage() {

            // console.log('修改表格');

            this.$router.push(`/editTable/${this.tableName}`); // 跳转到编辑页面，传递表格ID
            // console.log(this.tableName);

        },
        // 字段管理组件部分start
        openFieldManageDialog() {
            this.FieldManageDialogVisible = true;
            this.$refs.myFieldManage.fetchFields();
            
            
        },
        // end
        // 追加上传是否可用
        async IshasExistingData() {
            try {
                // 等待 Promise 返回并获取响应数据
                const response = await api.getSheets(this.tableName);
                // console.log('res', response);

                // 判断数据是否为空
                if (response.data.data.length !== 0) {
                    this.hasExistingData = true;  // 有数据，设置为 true
                } else {
                    this.hasExistingData = false;   // 无数据，设置为 false
                }

                // console.log('IshasExistingData', this.hasExistingData);

            } catch (error) {
                console.error("请求出错：", error);
                this.hasExistingData = false;  // 如果请求失败，默认设置为 false
            }

        },

        // 条件查询弹窗部分start
        handleClose(done) {
            if (this.loading) {
                return;
            }
            this.$confirm('确定要提交查询吗？')
                .then(res => {
                    // 验证查询条件
                    const validationMessage = this.$refs.myConditionalQuery.validateQueryConditions();
                    if (validationMessage !== true) {
                        this.$confirm(validationMessage, '错误', {
                            confirmButtonText: '确定',
                            type: 'error',
                            cancelButtonText: '',
                            showCancelButton: false,
                        });
                        return;
                    }
                    // 触发查询组件的构建查询条件的方法
                    this.$refs.myConditionalQuery.buildQueryConditions();
                    this.loading = true;
                    this.submitQuery(); // 提交查询逻辑
                    console.log(res, done);

                })
                .catch(res => {
                    console.log(res);

                });

                 // 将查询条件全部清空
                
        },
        handleButtonClick() {
            if (!this.loading) {
                // this.submitQuery(); // 提交查询逻辑
                this.$refs.drawer.closeDrawer(); // 关闭抽屉
            }
        },
        async submitQuery() {

            let filterConditions = this.queryConditions;

            // console.log("filterConditions", filterConditions);
            // 在这里处理查询条件,发送请求到后端 API
            try {
                const response = await api.filter({
                    ...filterConditions,
                    page: this.ConditionQueryResultCurrentPage, // 使用传入的 ConditionQueryResultCurrentPage
                    pageSize: this.ConditionQueryResultpageSize // 使用传入的 ConditionQueryResultpageSize
                });
                if (response.data.code === 0) {
                    // 将查询结果存入 Vuex store
                    this.$store.commit('SET_QUERY_RESULTS', response.data.data);
                    this.$emit('filter-results', response.data.data); // 触发事件并传递数据

                    

                } else {
                    this.$message.error(response.data.msg || '查询失败');
                }
            } catch (error) {
                this.$message.error('查询失败');
            } finally {
                this.loading = false;
                this.dialog = false;

                // 将查询条件全部清空，待补充

                this.timer = setTimeout(() => {
                    // done();
                    // 动画关闭需要一定的时间
                    setTimeout(() => {
                        this.loading = false;
                    }, 400);
                }, 2000);
                // console.log(res);
                this.$refs.myConditionalQuery.resetQueries();

            }
        },
        cancelForm() {

            clearTimeout(this.timer);
            this.$confirm('确定要取消查询吗？').then(res => {
                this.loading = false;
                this.dialog = false;


                this.timer = setTimeout(() => {
                    // done();
                    // 动画关闭需要一定的时间
                    setTimeout(() => {
                        this.loading = false;
                    }, 400);
                }, 2000);
                console.log(res);
                // 将查询条件全部清空
                this.$refs.myConditionalQuery.resetQueries();
            })
                .catch(res => {
                    console.log(res);

                });
        }
        // 条件查询部分end
    },
    mounted() {
        this.IshasExistingData();
        this.fetchSheetList();
    }

}
</script>

<style lang="scss" scoped>
// 标签
.tags {
    margin-left: 1.25rem
        /* 20/16 */
    ;
}

// 摘要字段
.mainContent {
    color: #409eff;
    font-weight: 700;
}

.OperationBox {

    // background-color: pink;
    padding: 1.875rem
        /* 30/16 */
        0;

    .ExcelOperationBox {
        display: flex;
        justify-content: flex-start;

        .DownloadfileBox {
            margin-left: 1.875rem
                /* 30/16 */
            ;
        }
    }

    .TableOperationBox {
        margin-top: 1.25rem
            /* 20/16 */
        ;
        // background-color: skyblue;
        display: flex;
        justify-content: flex-start;

        .EditInformation {
            margin-left: 1.875rem
                /* 30/16 */
            ;
        }

        .ConditionalQuery {
            margin-left: 1.875rem
                /* 30/16 */
            ;
        }
    }


}

// 上传/下载Excel文件下拉菜单的css开始
.el-dropdown {
    vertical-align: top;
}

.el-dropdown+.el-dropdown {
    margin-left: 15px;
}

.el-icon-arrow-down {
    font-size: 12px;
}

// 下拉菜单的css结束


// 条件查询的css开始
.my-demo-drawer {
    background-color: pink;
    width: 62.5rem
        /* 1000/16 */
    ;

    // .demo-drawer__content {
    //     background-color: skyblue;
    // }
    .conditional-query-button-group {
        display: flex;
        justify-content: space-around;

        .demo-drawer__footer {
            margin: 1.25rem
                /* 20/16 */
                0;
        }
    }

}
</style>