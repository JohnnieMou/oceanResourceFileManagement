import axios from 'axios';
import { Message } from 'element-ui';
import { API_BASE_URL, TIMEOUT, HEADERS } from './config';

// 创建axios实例
const instance = axios.create({
    baseURL: API_BASE_URL,
    timeout: TIMEOUT,
    headers: HEADERS,
});

// 请求拦截器
instance.interceptors.request.use(
    (config) => {
        // 从localStorage获取token
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['token'] = token;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
instance.interceptors.response.use(
    (response) => {
        if (response.data.code === -10101) {
            // 未登录，跳转到登录页
            Message.error(response.data.msg);
            window.location.href = '/login';
        } else {
            return response;
        }
    },
    (error) => {
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    // 未授权，跳转到登录页
                    Message.error('请重新登录');
                    window.location.href = '/#/login';
                    break;
                case 403:
                    Message.error('没有权限访问');
                    break;
                case 404:
                    Message.error('请求的资源不存在');
                    break;
                case 500:
                    Message.error('服务器错误');
                    break;
                default:
                    Message.error('网络错误');
            }
        } else {
            Message.error('网络连接失败');
        }
        return Promise.reject(error);
    }
);

export default instance;