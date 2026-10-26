<template>
    <div>
        <el-dialog :visible.sync="IsEditRecordDialogVisible" title="编辑记录" :append-to-body="true"
            :custom-class="'scrollable-dialog'" @close="handleClose" :modal="true" :lock-scroll="true">
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
            <div class="dialog-footer">
                    <el-button type="primary" @click="updateRecord" :loading="updateRecordLoading" class="dialog-footer">更新记录</el-button>
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
        sheetName: String,
        selectedEditRecord: Array
    },
    data() {
        return {
            updateRecordLoading: false,
            IsEditRecordDialogVisible: this.visible,
            RecordData: [],
            record: [] // 通过id获取的当前这条记录
        };
    },
    watch: {
        visible(newValue) {
            this.IsEditRecordDialogVisible = newValue;
            if (newValue) {
                this.fetchFields();
                // this.prefillRecordData(this.record);
            }
        },
        selectedEditRecord: {
            handler(newVal) {
                if (newVal) {
                    this.fetchRecordById();

                }
            },
            deep: true
        }
    },
    mounted() {
        this.fetchFields();
        // console.log('为什么没有');


        // console.log('编辑记录组件接收到的record:',this.record);
        // this.prefillRecordData(this.record);
    },
    methods: {
        handleClose() {
            this.$emit('update:visible', false);
        },
        async fetchRecordById() {
            try {
                const response = await api.fetchRecord(this.tableName, this.sheetName, this.selectedEditRecord[0]);
                // console.log('根据Id获取单条记录接口，返回值：',response);

                if (response.data.code === 0) {
                    this.record = response.data.data;
                    console.log('根据id获取的记录数据：', this.record);
                    this.prefillRecordData();
                } else {
                    this.$message.error(response.data.msg);
                }
            } catch (error) {
                console.error('根据id获取记录失败:', error);
                this.$message.error('根据id获取记录失败');
            }
        },
        async updateRecord() {
            try {
                this.updateRecordLoading = true;
                const recordData = {
                    id: this.selectedEditRecord[0],
                    record: {},
                    sheetName: this.sheetName,
                    tableName: this.tableName
                };
                // console.log('编辑记录组件，父组件传递过来的RecordId',this.selectedEditRecord[0]);
                this.RecordData.forEach(field => {
                    recordData.record[field.fieldName] = field.fieldValue;
                });
                // const recordData = {
                //     id: 3, 
                //     record: {
                //         '不含分数体育成绩': "99",
                //         '专业': "0",
                //         '品行表现测评': "63",
                //         '姓名': "陈锦宏",
                //         '学业表现测评': "75.10",
                //         '学号': "789900",
                //         '总成绩': 99,
                //         '班级': "软件1231",
                //         '科技与实践表现测评': "10.40"
                //     },
                //     sheetName: 'Sheet1',
                //     tableName: '示例表2'
                // };
                const response = await api.updateRecord(recordData);
                console.log("更新记录接口返回的值：", response);

                if (response.data.code === 0) {
                    this.$message.success('记录更新成功');
                    this.handleClose();
                    this.$emit('update-table-data');
                } else {
                    this.$message.error(response.data.msg);
                }
            } catch (error) {
                console.error('更新记录失败:', error);
                this.$message.error('更新记录失败');
            } finally {
                this.updateRecordLoading = false;
            }
        },
        async fetchFields() {
            try {
                const response = await api.getFields(this.tableName, this.sheetName);
                if (response.data.code === 0) {
                    this.RecordData = response.data.data.map(field => ({
                        ...field,
                        fieldValue: '',
                    }));
                    // console.log('调用getFields接口的返回：',response);
                    // console.log('调用fetchFields函数后的RecordData：',this.RecordData);
                    this.prefillRecordData();
                } else {
                    this.$message.error(response.data.msg);
                }
            } catch (error) {
                this.$message.error('获取字段失败');
            }
        },

        prefillRecordData() {
            if (!this.record) return;
            this.RecordData.forEach(field => {
                // 检查 this.record 中是否有与 field.fieldName 对应的属性
                if (field.fieldName in this.record) {
                    field.fieldValue = this.record[field.fieldName];
                } else {
                    // 如果 this.record 中没有对应的属性，可以设置一个默认值或者保持为空
                    field.fieldValue = '';
                }
            });
        }
    }
};
</script>
<style lang="scss" scoped>
.scrollable-dialog {
    .el-dialog__header {
    text-align: center; /* 标题居中 */
  }
    text-align: center;
    .el-dialog__body {
        
        height: 18.75rem /* 300/16 */;
        /* 设置对话框内容的最大高度 */
        overflow-y: auto;
        align-content: center;
        /* 允许垂直滚动 */
    }
    .dialog-footer {
        display: flex;
        justify-content: center; /* 水平居中 */
        align-items: center; /* 垂直居中 */
        margin-top: 20px;
        .el-button {
            margin: 0 5px; /* 按钮之间的间隔，如果需要的话 */
        }
    }
}

</style>