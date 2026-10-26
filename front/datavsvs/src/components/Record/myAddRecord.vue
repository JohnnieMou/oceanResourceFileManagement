<template>
    <div>
        <el-dialog :visible.sync="IsRecordDialogVisible" title="新增记录" width="30%" :modal="true" :lock-scroll="true" @close="handleClose">
            <div class="el-dialog__body">
                <el-table :data="RecordData">
                    <el-table-column prop="fieldName" label="字段名称">
                        <template slot-scope="scope">
                            {{ scope.row.fieldName }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="fieldValue" label="字段值">
                        <template slot-scope="scope">
                            <el-input v-model="scope.row.fieldValue"></el-input>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div style="margin-top: 20px;">
                <el-button type="primary" @click="addRecord" :loading="addRecordLoading">添加记录</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import api from "@/api/api";

export default {
    props: {
        visible: Boolean,
        tableName: String,
        sheetName: String
    },
    data() {
        return {
            addRecordLoading: false,
            IsRecordDialogVisible: this.visible,
            RecordData: [],

        };
    },
    watch: {
        visible(newValue) {
            this.IsRecordDialogVisible = newValue;
            if (newValue) {
                this.fetchFields();
            }
            // console.log(newValue);
        }
    },
    mounted() {
        this.fetchFields(this.tableName, this.sheetName)
    },
    methods: {
        // // 
        handleClose() {
            this.$emit('update:visible', false); // 通知父组件关闭弹窗
        },
        async addRecord() {
            try {
                this.addRecordLoading = true;
                // 构造请求体
                const recordData = {
                    record: {},
                    sheetName: this.sheetName,
                    tableName: this.tableName
                };

                // 从 RecordData 中收集字段值
                this.RecordData.forEach(field => {
                    recordData.record[field.fieldName] = field.fieldValue;
                });
                // console.log('要发送的recordData',recordData);
                // 调用 API 添加记录
                const response = await api.addRecord(recordData);
                console.log(response, '添加记录返回response');

                if (response.data.code === 0) {
                    this.$message.success('记录添加成功');
                    this.handleClose(); // 关闭弹窗
                    this.$emit('update-table-data'); // 派发事件，通知父组件
                } else {
                    this.$message.error(response.data.msg);
                }
            } catch (error) {
                console.error('添加记录失败:', error);
                this.$message.error('添加记录失败');
            } finally {
                this.addRecordLoading = false;
            }
        },
        // 获取字段名称，渲染表
        async fetchFields() {
            try {
                const response = await api.getFields(this.tableName, this.sheetName);
                if (response.data.code === 0) {
                    this.RecordData = response.data.data.map(field => ({
                        ...field,
                        isEditing: false,
                        originalName: field.fieldName,
                        originalType: field.fieldType
                    }));
                    // console.log(this.RecordData, 'RecordData');

                } else {
                    this.$message.error(response.data.msg);
                }
                //   console.log('表名',this.tableName);
                //     console.log('Sheet名：',this.sheetName);

                //     console.log('字段数据：',response);
                //     console.log('鬼打墙了');


            } catch (error) {
                this.$message.error('获取字段失败');
            }
        },
    }


};
</script>
<style scoped lang="scss">
.el-dialog__body {
        
        height: 18.75rem /* 300/16 */;
        /* 设置对话框内容的最大高度 */
        overflow-y: auto;
        align-content: center;
        /* 允许垂直滚动 */
    }
.dialog-footer {
  text-align: center;
}

</style>

