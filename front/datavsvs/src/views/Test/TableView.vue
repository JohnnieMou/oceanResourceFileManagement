<template>
    <div class="home">
        <div>这个页面废了，要删，但是先别删，留个底</div>
        <my-table-message></my-table-message>
        <div class="topbarBox">
            表格
        </div>
        <!-- 表格 -->
        <el-table :data="tableData" style="width: 100%">
            <!-- <el-table-column :prop="name" label="姓名">
            </el-table-column> -->
            <!-- 动态渲染表格列 -->
            <el-table-column v-for="(column, index) in tableColumns" :key="index" :prop="column.prop"
                :label="column.label">
            </el-table-column>
            <el-table-column label="操作" width="180">
                <template slot-scope="scope">
                    <el-button size="mini" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                    <el-button size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <!-- 分页组件 -->
        <my-pagination></my-pagination>
    </div>
</template>

<script>
import myTableMessage from '@/components/Table/myTableMessage.vue';
import MyPagination from '@/components/Public/myPagination.vue';
export default {
    components: { myTableMessage, MyPagination },
    data() {
        return {
            // 分页
            currentPage: 1,
            // 表格数据
            tableData: [],
            // 总数据量
            totalData: 0,
            // 动态生成的表格列
            tableColumns: []
        }
    },
    methods: {
        handleSizeChange(val) {
            console.log(`每页 ${val} 条`);
        },
        handleCurrentChange(val) {
            console.log(`当前页: ${val}`);
        },
        //   请求表格数据
        getTableData() {
            this.$axios
                .get('/api/sheet/page', {
                    params: {
                        page: 1,
                        pageSize: 10,
                        sheetName: 'Sheet2',
                        tableName: '示例表'
                    }
                })
                .then(res => {
                    // 更新表格数据
                    this.tableData = res.data.data.records;
                    this.totalData = res.data.data.total;

                    // 动态生成表格列
                    if (this.tableData.length > 0) {
                        // 获取第一条数据的键作为列名
                        this.tableColumns = Object.keys(this.tableData[0]).map(key => ({
                            prop: key,
                            label: key
                        }));
                    }
                }).catch(error => {
                    console.error('请求数据失败:', error);
                });
        },

    },
    created() {
        this.getTableData()
    },
    mounted() {
        // this.getTableData()
    }
}
</script>

<style lang="scss" scoped>
.home {
    box-sizing: border-box;
    
    .paginationBox {
        padding: 1.875rem
            /* 30/16 */
        ;
        // background-color: pink;
    }
}
</style>