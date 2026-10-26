<template>
    <div>
        <div class="box">
            <!-- 固定值查询框框 -->
            <div class="query-box" v-for="(item, index) in FixedValueQueryObj" :key="index">
                <div class="query-show-box">
                    <el-select size="mini" v-model="item.fieldName" placeholder="选择字段" class="slectfield">
                        <el-option v-for="(item, index) in FixedValueQueryOptions" :key="index" :label="item.label"
                            :value="item.value">
                        </el-option>
                    </el-select>
                    <el-input size="mini" v-model="item.inputFielVdValue" placeholder="字段值"
                        class="input-field-value"></el-input>
                </div>
                <el-button type="danger" icon="el-icon-delete" circle @click="deleteFixedValueQuery(index)"
                    class="deletebutton" size="mini"></el-button>
            </div>
            <!-- 添加固定值查询按钮 -->
            <el-button size="mini" type="primary" icon="el-icon-plus" @click="AddFixedValueQuery"
                class="add-query-box">添加固定值查询
            </el-button>

            <div style="margin-top: 1.25rem /* 20/16 */;">
                <!-- 范围查询框框 -->
                <div class="query-box" v-for="(item, index) in RangeQueryObj" :key="index">
                    <div class="query-show-box">
                        <el-select size="mini" v-model="item.fieldName" placeholder="选择字段" class="slectfield">
                            <el-option v-for="(item, index) in RangeValueQueryOptions" :key="index" :label="item.label"
                                :value="item.value">
                            </el-option>
                        </el-select>
                        <el-input size="mini" v-model="item.inputMinFielVdValue" placeholder="最小值"
                            class="input-min-field-value"></el-input>
                        <el-input size="mini" v-model="item.inputMaxFielVdValue" placeholder="最大值"
                            class="input-max-field-value"></el-input>
                    </div>
                    <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="deleteRangeQuery(index)"
                        class="deletebutton"></el-button>
                </div>
            </div>
            <!-- 添加范围值查询按钮 -->
            <el-button size="mini" type="primary" icon="el-icon-plus" @click="AddRangeValueQuery"
                class="add-query-box">添加范围查询
            </el-button>

            <div style="margin-top: 1.25rem /* 20/16 */;">
                <!-- 日期查询框框 -->
                <div class="query-box date-query-box" v-for="(item, index) in DateQueryObj" :key="index">
                    <div class="query-show-box">
                        <el-select size="mini" v-model="item.fieldName" placeholder="选择字段" class="slectfield"
                            style="width:12.5rem /* 200/16 */;">
                            <el-option v-for="(item, index) in DateValueQueryOptions" :key="index" :label="item.label"
                                :value="item.value">
                            </el-option>
                        </el-select>
                        <el-date-picker size="mini" v-model="item.inputStartDateValue" type="date" placeholder="开始日期"
                            class="el-date-picker" />
                        <el-date-picker size="mini" v-model="item.inputEndDateValue" type="date" placeholder="结束日期"
                            class="el-date-picker" />
                    </div>
                    <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="deleteDateQuery(index)"
                        class="deletebutton"></el-button>
                </div>
            </div>
            <!-- 添加日期查询按钮 -->
            <el-button size="mini" type="primary" icon="el-icon-plus" @click="AddDateValueQuery"
                class="add-query-box">添加日期查询
            </el-button>
            <!-- <el-button type="primary" @click="buildQueryConditions">执行查询</el-button> -->
        </div>
    </div>
</template>
<script>
import api from '@/api/api';

export default {
    emits: ['update-query-conditions'],
    props: {
        tableName: {
            type: String,
            required: true
        },
        sheetName: {
            type: String,
            required: true
        }
    },
    data() {
        return {
            // 固定值查询数组
            FixedValueQueryObj: [
                // {
                //     fieldName: '', // 初始值可以根据需求设定
                //     inputFielVdValue: '' // 初始输入值
                // }
            ],
            // 固定值查询渲染下拉框的数据，需要不同，因为需要根据类型禁用一些选项
            FixedValueQueryOptions: [],

            // 范围查询数组
            RangeQueryObj: [
                // {
                //     fieldName: '', // 初始值可以根据需求设定
                //     inputMinFielVdValue: '',// 最小值
                //     inputMaxFielVdValue: '',// 最小值
                // }
            ],
            // 范围查询渲染下拉框的数据，需要不同，因为需要根据类型禁用一些选项
            RangeValueQueryOptions: [],

            // // 日期查询数组
            DateQueryObj: [
                // {
                //     fieldName: '', // 初始值可以根据需求设定
                //     inputStartDateValue: '',// 开始值
                //     inputEndDateValue: '',// 开始值
                // }
            ],
            // // 日期查询渲染下拉框的数据，需要不同，因为需要根据类型禁用一些选项
            DateValueQueryOptions: [],
        }
    },
    mounted() {
        this.fetchQueryOptions();
    },
    methods: {
        // 验证查询条件是否完整
        validateQueryConditions() {
            // 检查固定值查询条件
            const hasFixedValueQuery = this.FixedValueQueryObj.length > 0;
            const allFixedValueConditionsFilled = hasFixedValueQuery && this.FixedValueQueryObj.every(item => item.fieldName && item.inputFielVdValue);

            // 检查范围查询条件
            const hasRangeQuery = this.RangeQueryObj.length > 0;
            const allRangeConditionsFilled = hasRangeQuery && this.RangeQueryObj.every(item => item.fieldName && item.inputMinFielVdValue && item.inputMaxFielVdValue);

            // 检查日期查询条件
            const hasDateQuery = this.DateQueryObj.length > 0;
            const allDateConditionsFilled = hasDateQuery && this.DateQueryObj.every(item => item.fieldName && item.inputStartDateValue && item.inputEndDateValue);

            // 检查是否至少有一种类型的查询条件已填写
            const hasAnyQuery = hasFixedValueQuery || hasRangeQuery || hasDateQuery;
            console.log(hasFixedValueQuery,'hasFixedValueQuery');
            console.log(hasRangeQuery,'hasRangeQuery');
            console.log(hasDateQuery,'hasDateQuery');
            console.log(hasAnyQuery,'hasAnyQuery');
            
            if (!hasAnyQuery) {
                return '您没有输入任何查询条件！';
            }
            console.log(allFixedValueConditionsFilled,'allFixedValueConditionsFilled');
            console.log(allRangeConditionsFilled,'allRangeConditionsFilled');
            console.log(allDateConditionsFilled,'allDateConditionsFilled');
            // console.log(hasAnyQuery,'hasAnyQuery');
            if (allFixedValueConditionsFilled||allRangeConditionsFilled||allDateConditionsFilled) {
                console.log('这样可以');
                
                // return true;
            }else{
                return '您没有输入完整的查询条件！';
            }

            return true;
        },
        // 清空所有查询条件
        resetQueries() {
            this.FixedValueQueryObj = [];
            this.RangeQueryObj = [];
            this.DateQueryObj = [];
        },
        async fetchQueryOptions() {
            try {
                const response = await api.getFields(this.tableName, this.sheetName);
                // console.log('myConditonalQuery组件，获取字段接口的返回结果:',response);

                if (response.data.code === 0) {
                    const fields = response.data.data;
                    this.FixedValueQueryOptions = fields.filter(field => field.fieldType === 'string'||field.fieldType === 'number').map(field => ({
                        label: field.fieldName,
                        value: field.fieldName
                    }));
                    this.RangeValueQueryOptions = fields.filter(field => field.fieldType === 'number').map(field => ({
                        label: field.fieldName,
                        value: field.fieldName
                    }));
                    this.DateValueQueryOptions = fields.filter(field => field.fieldType === 'date').map(field => ({
                        label: field.fieldName,
                        value: field.fieldName
                    }));
                } else {
                    this.$message.error(response.data.msg);
                }
            } catch (error) {
                this.$message.error('获取字段失败');
            }
        },
        buildQueryConditions() {

            const queryConditions = {
                fields: this.FixedValueQueryObj.map(item => item.fieldName),
                fieldValues: this.FixedValueQueryObj.map(item => item.inputFielVdValue),
                rangeFields: this.RangeQueryObj.map(item => item.fieldName),
                mins: this.RangeQueryObj.map(item => item.inputMinFielVdValue),
                maxs: this.RangeQueryObj.map(item => item.inputMaxFielVdValue),
                dateFields: this.DateQueryObj.map(item => item.fieldName),
                begin: this.DateQueryObj.map(item => item.inputStartDateValue),
                end: this.DateQueryObj.map(item => item.inputEndDateValue),
                tableName: this.tableName,
                sheetName: this.sheetName
            };
            this.$store.commit('SET_QUERY_CONDITIONS', queryConditions);

        },
        AddFixedValueQuery() {
            this.FixedValueQueryObj.push({
                fieldName: '',
                inputFielVdValue: ''
            });
        },
        deleteFixedValueQuery(index) {
            this.FixedValueQueryObj.splice(index, 1);
        },
        AddRangeValueQuery() {
            this.RangeQueryObj.push({
                fieldName: '',
                inputMinFielVdValue: '',
                inputMaxFielVdValue: ''
            });
        },
        deleteRangeQuery(index) {
            this.RangeQueryObj.splice(index, 1);
        },
        AddDateValueQuery() {
            this.DateQueryObj.push({
                fieldName: '',
                inputStartDateValue: '',
                inputEndDateValue: ''
            });
        },
        deleteDateQuery(index) {
            this.DateQueryObj.splice(index, 1);
        },
    }
}
</script>
<style lang="scss" scoped>
.box {
    // width: 100%;
    // height: 37.5rem
    //     /* 600/16 */
    // ;
    // background-color: pink;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;



    .query-box {
        width: 24.375rem
            /* 390/16 */
        ;
        height: 2.5rem
            /* 40/16 */
        ;

        position: relative;

        padding: .625rem
            /* 10/16 */
        ;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        border-radius: .25rem
            /* 4/16 */
        ;

        display: flex;
        justify-content: flex-start;
        align-items: center;

        .query-show-box {
            display: flex;
            justify-content: flex-start;
            align-items: center;
            width: 22.5rem
                /* 360/16 */
            ;
        }

        .slectfield {
            width: 40%;

        }

        .input-field-value {
            width: 50%;
            margin-right: .625rem
                /* 10/16 */
            ;
        }

        .deletebutton {
            position: absolute;
            right: .625rem
                /* 10/16 */
            ;
        }

        .input-min-field-value,
        .input-max-field-value {
            width: 30%;
        }

    }

    .add-query-box {
        margin-top: 1.25rem
            /* 20/16 */
        ;
        // background-color: #fff;
        width: 11.25rem
            /* 180/16 */
        ;
        margin-left: .625rem
            /* 10/16 */
        ;
    }
}
</style>