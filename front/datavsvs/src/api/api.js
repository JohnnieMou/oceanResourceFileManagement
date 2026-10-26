import request from './request';
import { setItem, removeItem, getItem } from '@/utils/storage';
const USER_URL = '/user';
const TABLE_URL = '/table';
const TAG_URL = '/tag';
const SHEET_URL = '/sheet';
const FIELD_URL = '/field';
const OPER_LOG_URL = '/operLog';
const RECORD_URL = '/record';
const NOTEBOOK_URL = '/notebook';
const Permission_URL = '/data-permission';

export default {
  // 权限管理start
   // 获取权限列表
   fetchPermission: async (page,pageSize) => {
    const response = await request.get(`${Permission_URL}/list`,{
      params: { page, pageSize }
    });
    return response;
  },
  // 添加权限
  addPermission: async (params) => {
    const response = await request.post(`${Permission_URL}/add`, params);
    return response;
  },
    // 编辑权限
  updatePermission: async (params) => {
    const response = await request.put(`${Permission_URL}/update`, params);
    // if (response.data.code === 0) {
    //   removeItem(`fields_${params.tableName}_${params.sheetName}`);
    // }
    return response;
  },
 // 根据用户Id获取未授权表列表
fetchListNoPermissionTables: async (userId) => {
  const response = await request.get(`${Permission_URL}/listNoPermissionTables/${userId}`);
  return response;
},
// 根据用户Id获取这个用户拥有的权限列表
 fetchPermissionListById: async (userId) => {
  const response = await request.get(`${Permission_URL}/getByUserId/${userId}`);
  return response;
},
  // 删除权限
deletePermission: async (params) => {
  const response = await request.delete(`${Permission_URL}/delete`, { data: params });
  return response;
},
  // 权限管理end

  // 记事本start
  // 获取记事本列表
  fetchNoteBook: async (page,pageSize) => {
    const response = await request.get(`${NOTEBOOK_URL}/list`,{
      params: { page, pageSize }
    });
    return response;
  },
  // 添加记事本
  addNoteBook: async (params) => {
    const response = await request.post(`${NOTEBOOK_URL}/add`, params);
    return response;
  },
  // 删除记事本
  deleteNoteBook: async (params) => {
    const response = await request.delete(`${NOTEBOOK_URL}/delete`, { params });
    return response;
  },
  // 更新记事本
  updateNoteBook: async (params) => {
    const response = await request.put(`${NOTEBOOK_URL}/update`, params);
    // if (response.data.code === 0) {
    //   removeItem(`fields_${params.tableName}_${params.sheetName}`);
    // }
    return response;
  },
  // 记事本end

  // 记录start
  // 添加记录
  addRecord: async (params) => {
    const response = await request.post(`${RECORD_URL}/add`, params);
    if (response.data.code === 0) {
      removeItem(`records_${params.tableName}_${params.sheetName}`);
    }
    return response;
  },
  // 删除记录
  deleteRecord: async (params) => {
    const response = await request.delete(`${RECORD_URL}/delete`, { data: params });
    if (response.data.code === 0) {
      removeItem(`records_${params.tableName}_${params.sheetName}`);
    }
    return response;
  },
  // 更新字段
  updateRecord: async (params) => {
    const response = await request.put(`${RECORD_URL}/update`, params);
    if (response.data.code === 0) {
      removeItem(`records_${params.tableName}_${params.sheetName}`);
    }
    return response;
  },
  // 根据id获取记录信息
  fetchRecord: async (tableName, sheetName, id) => {
    const response = await request.get(`${RECORD_URL}/getById`, {
      params: { tableName, sheetName, id }
    });
    return response;
  },
  // 记录end

  // 添加字段
  addField: async (params) => {

    const response = await request.post(`${FIELD_URL}/add`, params);
    // if (response.data.code === 0) {
    //   // removeItem(`fields_${params.tableName}_${params.sheetName}`);
    // }
    return response;

  },

  // 删除字段
  deleteField: async (params) => {

    const response = await request.delete(`${FIELD_URL}/delete`, { data: params });
    // if (response.data.code === 0) {
    //   // removeItem(`fields_${params.tableName}_${params.sheetName}`);
    // }
    return response;

  },

  // 更新字段
  updateField: async (params) => {

    const response = await request.put(`${FIELD_URL}/update`, params);
    // if (response.data.code === 0) {
    //   removeItem(`fields_${params.tableName}_${params.sheetName}`);
    // }
    return response;

  },

  // 获取字段列表
  getFields: async (tableName, sheetName) => {
    // const cacheKey = `fields_${tableName}_${sheetName}`;
    // const cachedFields = getItem(cacheKey);
    // if (cachedFields) {
    //   return Promise.resolve({ data: { code: 0, data: JSON.parse(cachedFields) } });
    // }


    const response = await request.get(`${FIELD_URL}/list`, {
      params: { tableName, sheetName }
    });
    // if (response.data.code === 0) {
    //   setItem(cacheKey, JSON.stringify(response.data.data));
    // }
    return response;

  },
  /* 用户模块 */
  // 登录
  login: (params) => {
    return request.post(`${USER_URL}/login`, params)

  },

  // 注册
  register: (params) => {
    return request.post(`${USER_URL}/register`, params);
  },
  // 获取用户信息,这里是给管理员展示所有用户的信息的，用于给权限和通过账号
  fetchUserInfo: (params) => {
    return request.get(`${USER_URL}/get`, params);
  },

  // 更新密码
  updatePassword: (params) => {
    // 确保params是一个对象，包含newPassword和oldPassword
    return request.put(`${USER_URL}/updatePassword`, null, { params });
  },

  // 更新用户信息
  updateUserInfo: async (params) => {

    const response = await request.put(`${USER_URL}/update`, params);
    // if (response.data.code === 0) {
    //   removeItem(`fields_${params.tableName}_${params.sheetName}`);
    // }
    return response;

  },
   // 删除用户
   deleteUser: async (params) => {
    const response = await request.delete(`${USER_URL}/delete`, { params });
    return response;
  },
  // 根据用户Id获取用户信息
  fetchUserInfoById: async (id) => {
    const response = await request.get(`${USER_URL}/get/${id}`);
    return response;
  },
  /* 表模块 */
  // 创建表
  createTable: async (params) => {
    const response = await request.post(`${TABLE_URL}/create`, params);
    return response;
  },

  // 获取表列表
  listTables: () => {
    return request.get(`${TABLE_URL}/list`);
  },

  // 上传文件（确认表字段类型）
  uploadConfirm: async (formData, tableName) => {
    const response = await request.post(`${TABLE_URL}/uploadConfirm/${tableName}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response;
  },

  // 上传文件（覆盖）
  uploadCover: async (fieldListMap, tableName) => {
    const response = await request.post(`${TABLE_URL}/uploadCover/${tableName}`, fieldListMap);
    if (response.data.code === 0) {
      removeItem(`sheets_${tableName}`);
    }
    return response;
  },

  // 上传文件（追加）
  uploadAppend: async (tableName) => {
    const response = await request.post(`${TABLE_URL}/uploadAppend/${tableName}`);
    if (response.data.code === 0) {
      removeItem(`sheets_${tableName}`);
    }
    return response;
  },

  // 下载Excel文件
  downloadExcel: (tableName) => {
    return request.post(`${TABLE_URL}/download`, null, {
      params: {
        tableName: tableName
      },
      responseType: 'blob',
    }).then(response => {
      // 使用 file-saver 库保存文件
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${tableName}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    });
  },

  // 下载指定sheet
  downloadSheet: (tableName, sheetName) => {
    return request.post(`${SHEET_URL}/download`, null, {
      params: {
        tableName: tableName,
        sheetName: sheetName
      },
      responseType: 'blob',
    }).then(response => {
      console.log("tableName:", tableName, " sheetName:", sheetName);

      // 使用 file-saver 库保存文件
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${tableName}___${sheetName}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    }).catch(error => {
      console.error('下载工作表时发生错误:', error);
      throw error; // 处理下载错误
    });
  },

  // 删除表
// 删除表
// 重要星星
deleteTable: async (tableName) => {
  // console.log('Deleting table:', tableName); // 调试信息
  const response = await request.delete(`${TABLE_URL}/${tableName.tableName}`);
  if (response.data.code === 0) {
    removeItem(`checkedTables`);
    removeItem(`sheets_${tableName}`);
    // 删除以fields_${tableName}_为前缀的所有localStorage项
    Object.keys(localStorage).forEach(key => {
      if (key.startsWith(`fields_${tableName}_`)) {
        removeItem(key);
      }
    });
  }
  return response;
},

  // 检查表是否存在
  checkTable: (tableName) => {
    return request.get(`${TABLE_URL}/check/${tableName}`);
  },

  // 获取表信息
  // getTableInfo: (tableName) => {
  //   return request.get(`${TABLE_URL}/info/${tableName}`);
  // },
  getTableInfo: (tableName) => {
    return request.get(`${TABLE_URL}/info?tableName=${encodeURIComponent(tableName)}`);
  },

  // 更新表
  updateTable: (updateTableDTO) => {
    removeItem(`checkedTables`);
    removeItem(`sheets_${updateTableDTO.oldTableName}`);
    return request.put(`${TABLE_URL}/update`, updateTableDTO);
  },

  // 创建标签
  createTag(tagName) {
    return request.post(`${TAG_URL}/create`, null, {
      params: { tagName }
    })
      .then(response => {
        if (response.data.code === 0) {
          removeItem('tags');
        }
        return response;
      })
      .catch(error => {
        console.error('创建标签失败:', error);
        throw error;
      });
  },

  // 删除标签
  deleteTag(tagName) {
    return request.delete(`${TAG_URL}/delete`, {
      params: { tagName }
    })
      .then(response => {
        if (response.data.code === 0) {
          removeItem('tags');
        }
        return response;
      })
      .catch(error => {
        console.error('删除标签失败:', error);
        throw error;
      });
  },

  // 更新标签
  updateTag(oldTagName, newTagName) {
    return request.put(`${TAG_URL}/update`, null, {
      params: { oldTagName, newTagName }
    })
      .then(response => {
        if (response.data.code === 0) {
          removeItem('tags');
        }
        return response;
      })
      .catch(error => {
        console.error('更新标签失败:', error);
        throw error;
      });
  },

  // 获取标签列表
  getTags() {
    // const cachedTags = getItem('tags');
    // if (cachedTags) {
    //   return Promise.resolve({ data: { code: 0, data: JSON.parse(cachedTags) } });
    // }
    return request.get(`${TAG_URL}/list`)
      .then(response => {
        if (response.data.code === 0) {
          setItem('tags', JSON.stringify(response.data.data));
        }
        return response;
      })
      .catch(error => {
        console.error('获取标签列表失败:', error);
        throw error;
      });
  },

  // 操作日志模块

  // 获取操作日志列表（分页）
  getOperationLogs(params) {
    return request.get(`${OPER_LOG_URL}/page`, { params: params })
      .then(response => response.data)  // 如果你需要返回响应数据
      .catch(error => {
        console.error('Error fetching operation logs:', error);
        throw error;  // 或者返回一个特定的错误消息
      });
  },

  // 清除操作日志
  clearOperationLogs() {
    return request.delete(`${OPER_LOG_URL}/clear`)
      .then(response => response.data)  // 返回响应数据
      .catch(error => {
        console.error('Error clearing operation logs:', error);
        throw error;
      });
  },

  /* Sheet模块 */
  // 获取工作表数据
  getSheets: async (tableName) => {
    const cacheKey = `sheets_${tableName}`;
    const cachedSheets = getItem(cacheKey);

    // 如果缓存存在，直接返回缓存数据
    if (cachedSheets) {
      return Promise.resolve({ data: { code: 0, data: JSON.parse(cachedSheets) } });
    }

    // 如果缓存不存在，则发送请求获取数据
    try {
      const response = await request.get(`${SHEET_URL}`, {
        params: { tableName },
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      });

      // 如果请求成功，缓存数据
      if (response.data.code === 0) {
        setItem(cacheKey, JSON.stringify(response.data.data));
      }
      return response;
    } catch (error) {
      console.error('获取工作表数据时发生错误:', error);
      throw error; // 抛出错误供调用者处理
    }
  },

  // 分页查询
  getPage: (params) => {
    return request.get(`${SHEET_URL}/page`, { params });
  },

  // 数值型列统计
  getStatistics: (tableName, sheetName) => {
    return request.post(`${SHEET_URL}/statistices`, null, {
      params: {
        tableName: tableName,
        sheetName: sheetName
      }
    });
  },

  // 条件查询
  filter: (params) => {
    return request.post(`${SHEET_URL}/filter`, params);
  },


};
