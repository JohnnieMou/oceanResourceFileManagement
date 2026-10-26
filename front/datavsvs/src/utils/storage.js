// 设置值到 localStorage
export const setItem = (key, value) => {
    localStorage.setItem(key, JSON.stringify(value));
  };
  
  // 从 localStorage 获取值
  export const getItem = (key) => {
    const value = localStorage.getItem(key);
    return value ? JSON.parse(value) : null;
  };
  
  // 从 localStorage 删除项
  export const removeItem = (key) => {
    localStorage.removeItem(key);
  };
  