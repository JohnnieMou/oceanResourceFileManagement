<template>
  <div>
    <el-table :data="statistics" style="width: 100%" v-loading="statisticsDataLoading" empty-text="暂无可统计字段">
      <el-table-column prop="columnName" label="列名" />
      <el-table-column prop="columnMax" label="最大值" />
      <el-table-column prop="columnMin" label="最小值" />
      <el-table-column prop="columnAvg" label="平均值" />
      <el-table-column prop="columnSum" label="总和" />
      <el-table-column prop="columnCount" label="计数" />
    </el-table>
  </div>

</template>

<script>
import api from '@/api/api'; // 确保这个路径与您的项目结构相匹配

export default {
  props: {
    tableName: {
      type: String,
      required: true,
      default: 'testTable'
    },
    sheetName: {
      type: String,
      required: true,
      default: 'Sheet1'
    }
  },
  data() {
    return {
      statistics: [],
      statisticsDataLoading:"true"
    };
  },
  
  mounted() {
    this.fetchStatistics();
  },
  methods: {
    fetchStatistics() {
      // console.log("this.tableName:", this.tableName);
      // console.log("this.sheetName:", this.sheetName);
      
      api.getStatistics(this.tableName, this.sheetName)
        .then(response => {
          // console.log('数值统计组件：',response);
          
          if (response.data.code === 0) {
            this.statistics = response.data.data;
          } else {
            // this.$message.error('获取统计信息失败');
            this.statistics = []; // 确保没有数据时，显示暂无数据
          }
        })
        .catch(error => {
          console.log("数值统计组件：",error);
          
          // this.$message.error('获取统计信息失败:',error);
          this.statistics = []; // 确保没有数据时，显示暂无数据
        })
        .finally(() => {
          this.statisticsDataLoading = false; 
        });
    }
  }
};
</script>

<style scoped lang="scss">
.statistics-container {
  margin-top: 20px;

  h3 {
    font-size: 1.2rem;
    margin-bottom: 10px;
    text-align: left;
  }
}
</style>