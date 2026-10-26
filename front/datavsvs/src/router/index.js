import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/HomeView.vue'
import TableDetailView from '@/views/Table/TableDetailView/TableDetailView';
import UserPermissionList from '@/views/Permission/UserPermissionsList.vue';
const routerPush = VueRouter.prototype.push;

const TableListView = () => import('../views/Table/TableListView/TableListView.vue');
// const TableView = () => import('../views/Test/TableView.vue');
const CreateTableView = () => import('../views/Table/CreateTableView/CreateTableView.vue');
const EditTableView = () => import('../views/Table//EditTableView/EdiTableView.vue');
const ManageTagsView = () => import('../views/Tag/ManageTagsView.vue');
const NoteBookList = () => import('../views/NoteBook/NoteBookList.vue');

const OperationLog = () => import('../views/OperationLog.vue');
const MapView = () => import('../views/Test/MapView.vue');
const Permission = () => import('../views/Test/PermissionView.vue');
const UserManage = () => import('../views/User/UserManage.vue');

// 测试的vue页面
const testView = () => import('../views/Test/testView.vue');



VueRouter.prototype.push = function (location) {

  return routerPush.call(this, location).catch(err => {
    console.log(err);

  })

};
Vue.use(VueRouter)

const routes = [
  {
    path: '/test',
    name: 'testView',
    component: testView
  },

  {
    path: '/home',
    name: 'home',
    component: HomeView,
    children: [
      {
        path: '/',
        name: 'TableListView',
        component: TableListView
      },
      {
        path: '/createTable',
        name: 'createTable',
        component: CreateTableView
      },
      {
        path: '/editTable/:tableName',
        name: 'editTableView',
        component: EditTableView
      },
      // 星星
      {
        path: '/table/:id',  // 动态路由，接收表格的 ID
        name: 'TableDetailView',
        component: TableDetailView,
        props: true  // 允许将路由参数作为 props 传递给组件
      },
      {
        path: '/userPermissionList/:id',  // 动态路由，接收表格的 ID
        name: 'UserPermissionList',
        component: UserPermissionList,
        props: true  // 允许将路由参数作为 props 传递给组件
      },
      {
        path: '/mapView',
        name: 'MapView',
        component: MapView
      },
      {
        path: '/operationLog',
        name: 'OperationLog',
        component: OperationLog
      },
      {
        path: '/permission',
        name: 'Permission',
        component: Permission
      },
      {
        path: '/usermanage',
        name: 'UserManage',
        component: UserManage
      },
      
      // {
      //   path: '/table',
      //   name: 'tableView',
      //   component: TableView
      // },

      {
        path: '/manageTags',
        name: 'manageTags',
        component: ManageTagsView
      },
      {
        path: '/noteBookList',
        name: 'noteBookList',
        component: NoteBookList
      },
      
    ]

  },

  // {
  //   path: '/OperationLog',
  //   name: 'operationLog',
  //   component: () => import('../views/OperationLog.vue')
  // }, 
  {
    path: '/login',
    name: 'loginView',
    component: () => import('../views/LoginAndRegister/LoginView/LoginView.vue')
  },
  {
    path: '/register',
    name: 'RegisterView',
    component: () => import('../views/LoginAndRegister/RegisterView/RegisterView.vue')
  },
  {
    path: '/',
    name: 'EntryView',
    component: () => import('../views/EntryView.vue')
  },
]

const router = new VueRouter({
  routes,
  // 添加 onSameUrlNavigation 配置项
  onSameUrlNavigation: 'ignore'
})
router.beforeEach((to, from, next) => {

  const token = localStorage.getItem('token')
  // if (!token) {
  //   next('/login');
  // }
  // console.log(token);
  
  // console.log('路由守卫有用到');
  
  if (to.path === '/home' && !token) {
    next('/login');
  }
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else {
    next()
  }
})
// 路由守卫
// 路由守卫
// router.beforeEach((to, from, next) => {
//   const token = localStorage.getItem('token');
//   if (to.path === '/' && !token) {
//     next('/login');
//   } else if (to.path === '/' && token) {
//     next('/'); // 如果已经登录，直接跳转到首页
//   } else {
//     next();
//   }
// });

export default router