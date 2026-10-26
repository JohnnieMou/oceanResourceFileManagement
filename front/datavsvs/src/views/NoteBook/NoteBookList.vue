<template>
    <div class="home">
      <el-breadcrumb separator-class="el-icon-arrow-right" class="breadCrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/noteBookList' }">记事本</el-breadcrumb-item>
      </el-breadcrumb>
  
      <div class="notebook">
        <div class="notes">
          <el-row class="note" v-for="note in notebook" :key="note.id">
            <div class="info">
              <div>
                <span>{{ note.updateUser }}</span>
                <span>{{ note.updateTime }}更新</span>
              </div>
              <div>
                <span @click="updateNote(note)">修改</span>
                <span @click="deleteNote(note.id)">删除</span>
              </div>
            </div>
            <div class="title">{{ note.title }}</div>
            <div class="content">{{ note.content }}</div>
          </el-row>
        </div>
  
        <div class="addNote">
          <el-button type="primary" icon="el-icon-plus" circle @click="addNoteBook"></el-button>
        </div>
      </div>
  
      <!-- 添加记事对话框 -->
      <AddNoteBook :visible.sync="addNoteInfoDialogVisible" :title="dialogTitle" @on-success="handleAddSuccess" />
  
      <!-- 修改记事对话框 -->
      <EditNoteBook :visible.sync="editNoteInfoDialogVisible" :title="editDialogTitle" :noteId="currentNote.id"
        :oldTitle="currentNote.title" :oldContent="currentNote.content" @on-success="handleEditSuccess" />
  
      <!-- 分页组件start -->
      <div class="pagination-container">
        <div v-if="notebook.length === 0" class="empty-message">
          目前这里还没有任何记事哦，快去添加吧！
        </div>
        <my-pagination
          v-else
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          @update:current-page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
      <!-- 分页组件end -->
  
    </div>
  </template>
  
  <script>
  import api from '@/api/api';
  import AddNoteBook from '@/components/NoteBook/addNoteBook.vue';
  import EditNoteBook from '@/components/NoteBook/editNoteBook.vue';
  import MyPagination from '@/components/Public/myPagination.vue';
  
  export default {
    components: {
      AddNoteBook,
      EditNoteBook,
      MyPagination,
    },
    data() {
      return {
        page: 1, // 当前页码
        pageSize: 10, // 每页显示的记录数
        total: 0, // 总记录数
        notebook: [], // 当前页的记事列表
  
        // 添加记事start
        addNoteInfoDialogVisible: false,
        dialogTitle: '添加记事',
        // 添加记事end
  
        // 修改记事start
        editNoteInfoDialogVisible: false,
        editDialogTitle: '修改记事',
        currentNote: {},
        // 修改记事end
      };
    },
    methods: {
      // 添加记事start
      addNoteBook() {
        this.addNoteInfoDialogVisible = true;
      },
      handleAddSuccess() {
        this.fetchNotebookList();
      },
      // 添加记事end
  
      async fetchNotebookList() {
        try {
          const response = await api.fetchNoteBook(this.page, this.pageSize);
          if (response.data.code === 0) {
            this.notebook = response.data.data.records; // 当前页的记录
            this.total = response.data.data.total; // 总记录数
            console.log('this.notebook:', this.notebook);
          } else {
            this.$message.error('获取记事列表失败: ' + response.data.msg);
          }
        } catch (error) {
          this.$message.error('获取记事列表异常: ' + error);
        }
      },
  
      // 修改记事start
      updateNote(note) {
        this.currentNote = note;
        this.editNoteInfoDialogVisible = true;
      },
      handleEditSuccess() {
        this.fetchNotebookList();
      },
      // 修改记事end

    //   删除记事start
      async deleteNote(id) {
        try {
          const confirmResult = await this.$confirm('此操作将永久删除该记事, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          });
          if (confirmResult === 'confirm') {
            const response = await api.deleteNoteBook({ id });
            if (response.data.code === 0) {
              this.$message.success('删除成功');
              this.fetchNotebookList();
            } else {
              this.$message.error('删除失败: ' + response.data.msg);
            }
          }
        } catch (error) {
          this.$message.info('已取消删除');
        }
      },
       //   删除记事end

      // 分页组件start
      handlePageChange(newPage) {
        this.page = newPage;
        this.fetchNotebookList();
      },
      handlePageSizeChange(newPageSize) {
        this.pageSize = newPageSize;
        this.page = 1; // 切换每页大小时重置为第一页
        this.fetchNotebookList();
      },
      // 分页组件end
    },
    mounted() {
      this.fetchNotebookList();
    },
  };
  </script>
  
  <style lang="scss" scoped>
  .home {
    color: #606266;
    box-sizing: border-box;
    padding: 1.875rem;
  
    .breadCrumb {
      margin-bottom: 1.25rem;
    }
  
    .notebook {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
  
      .notes {
        width: 100%;
  
        .note {
          border: 1px solid #ccc;
          border-radius: 15px;
          padding: 1.25rem;
          box-sizing: border-box;
          display: flex;
          flex-direction: column;
          justify-content: space-around;
          width: 100%;
          margin-bottom: 1.25rem;
  
          div {
            margin-bottom: 0.3125rem;
          }
  
          .title {
            text-align: left;
            font-size: 1rem;
            width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
  
          .content {
            text-align: left;
            font-size: 0.875rem;
          }
  
          .info {
            display: flex;
            justify-content: space-between;
  
            span {
              font-size: 0.875rem;
              margin-right: 0.833rem;
            }
          }
        }
      }
  
      .addNote {
        width: 1.875rem;
        height: 1.875rem;
        display: flex;
        justify-content: center;
        align-items: center;
        position: fixed;
        bottom: 8.333rem;
        right: 5rem;
      }
    }
  
    .pagination-container {
      margin-top: 20px;
      text-align: center;
  
      .empty-message {
        color: #999;
        font-size: 16px;
        margin-top: 20px;
        text-align: center;
      }
    }
  }
  </style>