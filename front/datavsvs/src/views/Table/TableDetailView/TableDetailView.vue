<template>
    <div>
        <div class="home">
            <!-- 面包屑导航 -->
            <el-breadcrumb separator-class="el-icon-arrow-right" class="breadCrumb">
                <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item :to="{ path: '/home' }">选择表</el-breadcrumb-item>
                <el-breadcrumb-item>{{ propTableData.tableName }}</el-breadcrumb-item>
            </el-breadcrumb>

            <!-- 表格信息展示 -->
            <my-table-message :tableName="propTableData.tableName" :description="propTableData.description"
                :tags="propTableData.tags" :currentPage="currentPage" :pageSize="pageSize"
                :ConditionQueryResultpageSize="ConditionQueryResultpageSize"
                :ConditionQueryResultCurrentPage="ConditionQueryResultCurrentPage"
                @filter-results="handleFilterResults">
            </my-table-message>
            <div v-loading="MainTableDataLoading" class="table">
                <el-button @click="addRecord" type="primary" class="addRecord">新增记录</el-button>
                <!-- 空数据提示 -->
                <div v-if="tableData.length === 0" class="empty-message">
                    还未上传表，请覆盖上传
                </div>

                <!-- 表格数据渲染 -->
                <div v-if="tableData.length > 0">
                    <el-table :data="tableData">
                        <!-- 动态渲染表格列 -->
                        <el-table-column v-for="(column, index) in tableColumns" :key="index" :prop="index.toString()"
                            :label="column">
                        </el-table-column>

                        <!-- 操作列 -->
                        <el-table-column label="操作" width="180" fixed="right">
                            <template slot-scope="scope">
                                <el-button size="mini" @click="EditRecord(scope.$index, scope.row)">编辑</el-button>
                                <el-button size="mini" type="danger"
                                    @click="DeleteRecord(scope.$index, scope.row)">删除</el-button>
                            </template>
                        </el-table-column>
                    </el-table>

                    <!-- 分页组件 -->
                    <my-table-pagination :current-page="currentPage" :page-size="pageSize" :total="total"
                        @update:current-page="handleCurrentPageChange" @update:page-size="handlePageSizeChange" />


                    <!-- <my-pagination 
                    :current-page="currentPage" :page-size="pageSize" :total="total"
                        @update:current-page="handleCurrentPageChange" @update:page-size="handlePageSizeChange" 
                        /> -->
                </div>
            </div>
            <!-- 添加记录弹窗组件 -->
            <my-add-record :visible.sync="IsAddRecordDialogVisible" :tableName="propTableData.tableName"
                :sheetName="currentSheetName" @update-table-data="getTableData"></my-add-record>
            <!-- 编辑记录弹窗组件 -->
            <my-edit-record :visible.sync="IsEditRecordDialogVisible" :tableName="propTableData.tableName"
                :sheetName="currentSheetName" :selectedEditRecord="selectedEditRecord" @update-table-data="getTableData"
                ref="editRecordComponent"></my-edit-record>
        </div>
    </div>
</template>

<script>
import { mapMutations } from 'vuex'; // 引入 Vuex 的 mapMutations
import api from '@/api/api';
import myTableMessage from '@/components/Table/myTableMessage.vue';
// import MyPagination from '@/components/Public/myPagination.vue';
import MyAddRecord from '@/components/Record/myAddRecord.vue';
import MyEditRecord from '@/components/Record/myEditRecord.vue';
import MyTablePagination from '@/components/Table/myTablePagination.vue';

export default {
    props: ['id'],
    components: { myTableMessage, MyAddRecord, MyEditRecord, MyTablePagination },
    data() {
        return {
            MainTableDataLoading: 'false',
            propTableData: {
                tableName: '',
                description: '',
                tags: []

            },
            currentSheetName: "Sheet1",
            // 星星，这里想修改，从获取表信息的接口获取，不直接从路径获取
            // propTableData: {
            //     tableName: this.$route.query.tableName,
            //     description: "",
            //     tags: "",
            // },
            total: 0,
            pageSize: 10,       // 每页显示数据条数
            currentPage: 1,
            tableData: [],      // 表格数据
            tableColumns: [],   // 表格列配置

            // 记录部分start
            IsAddRecordDialogVisible: false,
            IsEditRecordDialogVisible: false,
            selectedEditRecord: null, // 用于存储需要编辑的记录
            // 记录部分end

            // 条件查询部分start
            IsConditionQueryResult: false,//标志是否是条件查询的结果
            ConditionQueryResultpageSize: 10,
            ConditionQueryResultCurrentPage: 1,
            IsPageOrPageSizeChange: true,//标志是否是分页变化导致的查询，如果是，不用提示查询成功，如果不是（第一次查询，提示查询成功）
            // 条件查询部分end


        };
    },
    created() {
        this.fetchTableMessage();

    },
    mounted() {
        // 获取表格详情页的路径Id start
        // 获取上一个页面的路由路径
        const previousPath = this.$route.redirectedFrom || this.$route.path;
        console.log("表格详情页的上一个页面的路由路径:", previousPath);

        // 提取 /table/ 后面的数字
        const detailTableId = this.extractTableId(previousPath);

        // 将 detailTableId 存入 Vuex store
        this.setDetailTableId(detailTableId);

        // 获取表格详情页的路径Id end

        this.MainTableDataLoading = "ture",
            this.fetchTableMessage();
        this.getTableData();  // 在组件挂载时获取表格数据

    },
    methods: {

        //  获取表格详情页的路径Id start
        ...mapMutations(['setDetailTableId']), // 映射 Vuex 的 mutations

        // 提取 /table/ 后面的数字
        extractTableId(path) {
            const regex = /\/table\/(\d+)/; // 正则表达式匹配 /table/ 后面的数字
            const match = path.match(regex);
            if (match && match[1]) {
                return match[1];
            }
            return null; // 如果没有匹配到，返回 null
        },
    
    //  获取表格详情页的路径Id end

    // 条件查询start
    // // 用了查询功能之后的数据更新
    handleFilterResults(data) {
        this.IsConditionQueryResult = true;
        // console.log('查询出来的结果：',data);
        this.tableData = data.records.map(item => Object.values(item)); // 设置表格数据

        // console.log("处理后的结果",this.tableData);
        this.total = data.total; // 更新总条数
        console.log('怎么不触发呢怎么不触发呢怎么不触发呢怎么不触发呢怎么不触发呢');

        console.log('this.IsPageOrPageSizeChange', this.IsPageOrPageSizeChange);

        // this.IsPageOrPageSizeChange=true;

        if (this.IsPageOrPageSizeChange) {
            this.$confirm('查询成功', '提示', {
                confirmButtonText: '确定',
                type: 'success',
                cancelButtonText: '',
                showCancelButton: false,
            })
        }

    },
    // 处理分页大小变化
    handlePageSizeChange(val) {
        if (this.IsConditionQueryResult) {
            // console.log('现在应该处理条件查询结果的分页');
            this.ConditionQueryResultpageSize = val;
            this.IsPageOrPageSizeChange = false;
        } else {
            // console.log(`每页 ${val} 条`);
            this.pageSize = val;
            this.getTableData(); // 获取当前页的数据
        }
    },

    // 处理页码变化
    handleCurrentPageChange(val) {

        if (this.IsConditionQueryResult) {
            // console.log('现在应该处理条件查询结果的分页');
            this.ConditionQueryResultCurrentPage = val;
            this.IsPageOrPageSizeChange = false;
        } else {
            // console.log(`当前页: ${val}`);
            this.currentPage = val;
            this.getTableData(); // 获取当前页的数据
        }

    },
    // 条件查询end

    // 记录部分start
    addRecord() {
        this.IsAddRecordDialogVisible = true;
    },
    // 编辑操作
    EditRecord(index, row) {
        // console.log('编辑行:', index, row);
        this.selectedEditRecord = row; // 设置需要编辑的记录
        this.IsEditRecordDialogVisible = true; // 显示编辑记录的弹窗
    },

    // 删除操作
    async DeleteRecord(index, row) {

        try {
            const params = {
                id: row[0],
                tableName: this.propTableData.tableName,
                sheetName: this.currentSheetName
            };
            const response = await api.deleteRecord(params);
            console.log('删除记录的接口返回：', response);

            if (response.data.code === 0) {
                this.$message.success('记录删除成功');
                this.getTableData(); // 重新获取表格数据
            } else {
                this.$message.error('删除记录失败:', response.data.msg);
            }
        } catch (error) {
            console.error('删除记录失败:', error);
            this.$message.error('删除记录失败');
        }
    },
    // 记录部分end
    // 从路由的路径解码获取的表格信息
    fetchTableMessage() {
        // 路由参数是通过 query 传递的
        const tableName = this.$route.query.tableName;
        const description = this.$route.query.description;
        const tagsString = this.$route.query.tags;

        // 解码并赋值
        this.propTableData.tableName = decodeURIComponent(tableName);
        this.propTableData.description = decodeURIComponent(description);
        this.propTableData.tags = JSON.parse(decodeURIComponent(tagsString));
        // console.log("tableName", this.propTableData.tableName);
    },

    async getTableData() {
        const params = {
            page: this.currentPage,
            pageSize: this.pageSize,
            tableName: this.propTableData.tableName,
            sheetName: 'Sheet1',//星星，需要替换，搞清楚sheet的机制先
        };

        try {
            const response = await api.getPage(params);
            // console.log(response); // 打印响应数据，检查是否正确

            if (response.data.code === 0) {
                this.total = response.data.data.total;
                const data = response.data.data;
                // console.log('数据:', data); // 打印返回的数据
                this.updatetableData(data);
            }
        } catch (error) {
            console.error('获取表格数据时发生错误:', error);
            this.$message.error('获取数据失败');
        } finally {
            this.MainTableDataLoading = false; // 不管成功或失败，都将loading设为false
        }
    },
    // 数据更新方法
    updatetableData(data) {
        // 存储原始数据
        // this.tableData = data;

        // 获取 records 数据
        const records = data.records || [];

        // 如果 records 中有数据，开始处理
        if (records.length > 0) {


            // 使用第一条记录的键作为表头
            this.tableColumns = Object.keys(records[0]);

            // console.log('tableColumns', this.tableColumns);


            // 所有记录都作为数据展示，按键的下标顺序
            this.tableData = records.map(function (item) {
                return Object.values(item);
            });
            // console.log(this.tableData);

        }
    }
},
};
</script>

<style lang="scss" scoped>
.home {
    box-sizing: border-box;
    padding: 1.875rem;

    .breadCrumb {
        margin-bottom: 1.25rem;
    }

    .empty-message {
        text-align: center;
        color: #999;
        font-size: 16px;
        margin-top: 20px;
        font-weight: bold;
    }

    .table {
        // position: relative;

        .addRecord {
            float: right;
            margin-top: 1.25rem;
            /* 调整为需要的值 */
            margin-right: 1.25rem;
            /* 调整为需要的值 */
        }
    }

}
</style>