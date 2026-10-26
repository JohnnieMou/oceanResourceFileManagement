import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    activePath: '/', // 默认激活的路径
    isSidebarVisible: false,// 控制侧边导航栏的显示

    // 条件查询部分start
    queryConditions: {//从myConditionalQuery组件收集的，要用于发送的查询条件
      fields: [],
      fieldValues: [],
      rangeFields: [],
      mins: [],
      maxs: [],
      dateFields: [],
      begin: [],
      end: [],
      tableName: '',
      sheetName: ''
    },
    queryResults: null, // 存储查询结果
    // 条件查询部分end

    // 获取上一个页面的路由路径start
    detailTableId: null, // 用于存储 detailTableId
    // 获取上一个页面的路由路径end

  },
  mutations: {
    updateActivePath(state, path) {
      state.activePath = path
    },
    toggleSidebar(state, isVisible) {
      state.isSidebarVisible = isVisible
    },
    // 条件查询部分start
    SET_QUERY_CONDITIONS(state, payload) {
      state.queryConditions = payload
    },
    SET_QUERY_RESULTS(state, payload) {
      state.queryResults = payload
    },
    // 条件查询部分end

    // 获取上一个页面的路由路径start
    setDetailTableId(state, detailTableId) {
      state.detailTableId = detailTableId
    },
    // 获取上一个页面的路由路径end

  },
  getters: {
    // 获取上一个页面的路由路径start
    getDetailTableId: (state) => {
      return state.detailTableId
    },
    // 获取上一个页面的路由路径end

    // 其他 getters...
  }
})