<template>
  <div>
    <div class="home">
      <!--面包屑导航 -->
      <el-breadcrumb separator-class="el-icon-arrow-right" class="breadCrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home' }">选择表</el-breadcrumb-item>
      </el-breadcrumb>
      <!-- 表列表 -->
      <el-table :data="tableData" style="width: 100%" border v-loading="tableListLoading">
        <!-- <el-table-column type="selection" width="55"></el-table-column> -->
        <el-table-column prop="tableName" label="表名"></el-table-column>
        <el-table-column prop="description" label="概要"></el-table-column>
        <el-table-column prop="tags" label="标签" :filters="tagFilters" :filter-method="filterByTags">
          <template slot-scope="scope">
            <!-- 渲染每个标签 -->
            <el-tag class="tags" v-for="(tag, index) in scope.row.tags" :key="index">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleInto(scope.$index, scope.row)">进入</el-button>
            <el-button size="mini" @click="handleEdit(scope.$index, scope.row, 'indexHomeEdit')">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <my-pagination :current-page="currentPage" :page-size="pageSize" :total="total"
        @update:current-page="handleCurrentPageChange" @update:page-size="handlePageSizeChange" />
    </div>
  </div>
</template>

<script>
import myPagination from '@/components/Public/myPagination.vue';
import api from '@/api/api';
import { MessageBox } from 'element-ui';

export default {
  components: { myPagination },

  created() {
    this.getlistTables();
  },

  data() {
    return {
      tableListLoading:false,
      // 分页数据
      currentPage: 1,
      pageSize: 10,
      total: 0,

      // 表格数据
      allTables: [],  // 存储所有表格数据
      tableData: [],  // 当前页展示的数据
      // 可筛选的标签列表（从数据动态生成）
      tagFilters: [
      ]
    };
  },

  methods: {

    // 标签筛选方法
    filterByTags(value, row) {
      // 如果当前行的 tags 中包含筛选的标签，返回 true
      return row.tags.includes(value);
    },
    // 修改 handleEdit 函数，跳转到编辑页面
    handleEdit(index, row, type) {
      // console.log('row', row);
      // console.log('row.id:', index);
      this.$router.push(`/editTable/${row.tableName}`); // 跳转到编辑页面，传递表格ID
      if (type === 'indexHomeEdit') {
        console.log(type);
      }

    },

    // 删除表格
    async handleDelete(index, row) {
      // 获取表名
      const tableName = row.tableName;
      console.log('tableName:',tableName);
      // 弹出确认删除的提示框
      MessageBox.confirm(`确定要删除表格 "${tableName}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          
          
          // 调用 deleteTable 方法来删除表格
          const response = await api.deleteTable({tableName});
          console.log('删除表的response:',response);
          
          // 如果删除成功
          if (response.data.code === 0) {
            // 从 allTables 中删除该条数据
            this.allTables = this.allTables.filter(table => table.tableName !== tableName);

            // 更新总数
            this.total = this.allTables.length;

            // 如果当前页的数据为空，且不是最后一页，跳转到上一页
            if (this.tableData.length === 1 && this.currentPage > 1) {
              this.currentPage -= 1;
            }

            // 更新当前页的数据
            this.updatePagedData();
            this.$message.success('删除表格成功');
          }
        } catch (err) {
          console.error('删除表格失败', err);
        }
      }).catch(() => {
        // 如果用户点击了取消按钮
        console.log('删除操作已取消');
      });
    },
    // 调用获取表列表接口
    async getlistTables() {
      try {
        this.tableListLoading=true;
        const response = await api.listTables();

        // 更新表格数据和总数
        this.allTables = response.data.data;
        this.total = this.allTables.length;
        // 初次加载时，调用分页处理函数
        this.updatePagedData();
      } catch (err) {
        console.error('获取表格数据失败', err);
      }finally{
        this.tableListLoading=false;
      }
    },

    // 根据当前页和每页条数来更新当前页的数据
    updatePagedData() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = this.currentPage * this.pageSize;
      this.tableData = this.allTables.slice(start, end);

      // 1. 获取所有当前页中的标签（去重）
      const allTags = this.tableData.reduce((tags, row) => {
        row.tags.forEach(tag => {
          if (!tags.includes(tag)) {
            tags.push(tag); // 如果没有重复的标签，则添加
          }
        });
        return tags;
      }, []);

      // 2. 为每个标签生成筛选项
      this.tagFilters = allTags.map(tag => {
        return { text: tag, value: tag }; // value 和 text 都是标签内容
      });
      // console.log('this.currentPage',this.currentPage);
      // console.log('this.pageSize',this.pageSize);

      // console.log('tableData',this.tableData);

    },

    // 更新当前页
    handleCurrentPageChange(page) {
      this.currentPage = page;
      this.updatePagedData();  // 更新当前页的数据
    },
    // 更新每页显示条数
    handlePageSizeChange(size) {
      this.pageSize = size;
      this.updatePagedData();  // 更新当前页的数据
    },


    // 点击进入对应的表格
    handleInto(res1, res2) {

      
      // console.log('res1:', res1);

      // console.log('res2:', res2);

      const tableId = res1;
      const tableName = res2.tableName;
      const description = res2.description;
      const tags = res2.tags;

      this.$router.push({
        path: `/table/${tableId}`,
        query: {
          // tableName,
          // description,
          // tags: JSON.stringify(tags),
          tableName: encodeURIComponent(tableName),
          description: encodeURIComponent(description),
          tags: encodeURIComponent(JSON.stringify(tags))
        },
      });
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

  .tags {
    margin-right: .3125rem
      /* 5/16 */
    ;
    // background-color: skyblue;
  }
}
</style>
