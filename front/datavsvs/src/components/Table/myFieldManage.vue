<template>
    <el-dialog :visible.sync="dialogVisible" title="字段管理" @close="handleClose" :modal="true" :lock-scroll="true">
        <div class="el-dialog__body">
            <el-table :data="fieldsData" v-loading="fetchFieldsLoading">
                <el-table-column prop="fieldName" label="字段名称">
                    <template slot-scope="scope">
                        <el-input v-model="scope.row.fieldName" :disabled="!scope.row.isEditing"></el-input>
                    </template>
                </el-table-column>
                <el-table-column prop="fieldType" label="数据类型">
                    <template slot-scope="scope">
                        <span v-if="!scope.row.isEditing">{{ getChineseFieldType(scope.row.fieldType) }}</span>
                        <el-select v-else v-model="scope.row.fieldType">
                            <el-option label="字符串" value="string"></el-option>
                            <el-option label="数字" value="number"></el-option>
                            <el-option label="日期" value="date"></el-option>
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template slot-scope="scope">
                        <div class="FieldManageButtonGrop">
                            <el-button v-if="!scope.row.isEditing" size="small"
                                @click="editField(scope.$index)">编辑</el-button>
                            <el-button v-if="scope.row.isEditing" size="small" type="success"
                                @click="saveField(scope.$index)">保存</el-button>
                            <el-button v-if="!scope.row.isEditing" size="small" type="danger"
                                @click="deleteField(scope.$index)">删除</el-button>
                            <el-button v-if="scope.row.isEditing" size="small"
                                @click="cancelEdit(scope.$index)">取消</el-button>
                        </div>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <div style="margin-top: 20px;">
            <el-button type="primary" @click="addField">添加字段</el-button>
        </div>
    </el-dialog>
</template>

<script>
import api from "@/api/api";
export default {
    props: {
        visible: Boolean,
        tableName: String,
        sheetName: String
    },
    mounted() {
        this.fetchFields();
    },
    data() {
        return {
            fetchFieldsLoading: false,
            dialogVisible: this.visible,
            fieldsData: [],
            fieldTypeMap: {
                'string': '字符串',
                'number': '数字',
                'date': '日期'
            }
        };
    },
    watch: {
        visible(newValue) {
            this.dialogVisible = newValue;
            if (newValue) {
                this.fetchFields();
            }
            // console.log(newValue);

        }
    },

    methods: {
        // 
        handleClose() {
            this.$emit('update:visible', false); // 通知父组件关闭弹窗
            // 成功后刷新整个页面
            // 星星，以后必须修改
            window.location.reload();  // 刷新页面
        },
        // 
        editField(index) {
            const field = this.fieldsData[index];
            // 进入编辑状态
            field.isEditing = true;
        },
        getChineseFieldType(englishType) {
            return this.fieldTypeMap[englishType] || englishType;
        },
        async fetchFields() {
            this.fetchFieldsLoading = true;
            // console.log('有无被调用啊');

            try {
                const response = await api.getFields(this.tableName, this.sheetName);
                // console.log('字段获取这里怎么总出问题',response);

                if (response.data.code === 0) {
                    this.fieldsData = response.data.data.map(field => ({
                        ...field,
                        isEditing: false,
                        originalName: field.fieldName,
                        originalType: field.fieldType
                    }));
                } else {
                    // this.$message.error(response.data.msg);
                    // this.$message.error('是你吗2333');   
                }
            } catch (error) {
                // this.$message.error('是你吗');
                console.log('获取字段接口', error);

            } finally {
                this.fetchFieldsLoading = false;
            }
        },
        async addField() {
            const newField = {
                fieldName: '',
                fieldType: 'string',
                isEditing: true,
                originalName: '',
                originalType: ''
            };
            this.fieldsData.push(newField);
        },
        async saveField(index) {
            console.log("index", index);

            const field = this.fieldsData[index];
            // console.log('要修改的字段：',field);

            try {
                if (field.originalName) {
                    // 更新字段
                    // console.log(this.tableName);
                    // console.log(this.sheetName);
                    // console.log('旧字段名',field.originalName);
                    // console.log('新字段名',field.fieldName);
                    // console.log('要改成的字段类型',field.fieldType);
                    if (field.originalName === field.fieldName) {
                        await api.updateField({
                            tableName: this.tableName,
                            sheetName: this.sheetName,
                            oldFieldName: field.originalName,
                            newFieldName: '',
                            fieldType: field.fieldType
                        });
                    }
                    await api.updateField({
                        tableName: this.tableName,
                        sheetName: this.sheetName,
                        oldFieldName: field.originalName,
                        newFieldName: field.fieldName,
                        fieldType: field.fieldType
                    });                    // console.log('更新字段返回的数据：', res);

                } else {
                    await api.addField({
                        fieldName: field.fieldName,
                        fieldType: field.fieldType,
                        tableName: this.tableName,
                        sheetName: this.sheetName
                    });
                }

                field.isEditing = false;
                field.originalName = field.fieldName;
                field.originalType = field.fieldType;
                // console.log('字段的新名',field.originalName);
                // console.log('字段的新类型',field.originalType);

                this.$confirm('字段保存成功', '提示', {
                    confirmButtonText: '确定',
                    type: 'success',
                    showCancelButton: false,
                })
            } catch (error) {
                this.$message.error('保存字段失败');
                console.error('保存字段失败', error);
            }
        },
        async deleteField(index) {
            const field = this.fieldsData[index];
            try {
                await api.deleteField({
                    fieldName: field.fieldName,
                    tableName: this.tableName,
                    sheetName: this.sheetName
                });
                this.fieldsData.splice(index, 1);
                this.$message.success('字段删除成功');
            } catch (error) {
                this.$message.error('删除字段失败');
                console.error('删除字段失败', error);
            }
        },
        cancelEdit(index) {
            const field = this.fieldsData[index];
            if (field.originalName) {
                field.fieldName = field.originalName;
                field.fieldType = field.originalType;
                field.isEditing = false;
            } else {
                this.fieldsData.splice(index, 1);
            }
        }
    },

}



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
.FieldManageButtonGrop {
    display: flex;
}
</style>
