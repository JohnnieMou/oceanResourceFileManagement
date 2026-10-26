<template>
    <div>
      <!-- 分页组件 -->
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100 ,200]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </template>
  
  <script>
  export default {
    props: {
      // 父组件传递的分页数据
      currentPage: {
        type: Number,
        default: 1,
      },
      pageSize: {
        type: Number,
        default: 10,
      },
      total: {
        type: Number,
        default: 0,
      },
    },
  
    methods: {
      // 页面大小变化
      handleSizeChange(val) {
        this.$emit('update:page-size', val); // 更新父组件的 pageSize
      },
  
      // 当前页变化
      handleCurrentChange(val) {
        this.$emit('update:current-page', val); // 更新父组件的 currentPage
      },
    },
  };
  </script>
  
  <style scoped>
  .paginationBox {
    margin-top: 20px;
  }
  </style>
  