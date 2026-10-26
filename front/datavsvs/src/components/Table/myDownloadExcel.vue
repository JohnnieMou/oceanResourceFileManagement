<template>
    <div class="DownloadfileBox">
        <el-dropdown @command="handleDownload">
            <el-button type="primary">
                下载Excel文件
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
                <el-dropdown-menu>
                    <el-dropdown-item command="Excel">下载整个Excel</el-dropdown-item>
                    <el-dropdown-item command="Sheet">下载当前sheet</el-dropdown-item>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
    </div>
</template>

<script>
import api from '@/api/api';
export default {
    name: "MyDownloadExcel",
    props: {
        tableName: {
            type: String,
            required: true,
        },
        sheetName: {
            type: String,
            required: false,
        },
    },
    data() {
        return {
            downLoadType: '',
        };
    },
    methods: {

        handleDownload(command) {
            this.downLoadType = command;
            if (this.downLoadType === 'Excel') {
                this.downloadExcel()
            } else if (this.downLoadType === 'Sheet') {
                this.downloadCurrentSheet()
            }
        },
        // 下载整个 Excel 文件
        downloadExcel() {
            const res = api.downloadExcel(this.tableName).then(() => {
                this.$message.success("下载成功");
            }).catch(() => {
                this.$message.error("下载失败");
            });
            console.log(res);
            console.log('click1');
        },

        // 下载当前 sheet
        downloadCurrentSheet() {
            if (!this.sheetName) {
                this.$message.warning("当前没有指定的Sheet");
                return;
            }
            const res2 = api.downloadSheet(this.tableName, this.sheetName).then(() => {
                this.$message.success("下载成功");
            }).catch(() => {
                this.$message.error("下载失败");
            });
            console.log(res2);
            console.log('click2');

        },
    },
};
</script>

<style scoped>
/* .DownloadfileBox {
    margin: 10px;
} */
</style>