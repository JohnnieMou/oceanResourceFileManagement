const config = {
    // 开发环境API地址（反向代理会生效）
    development: {
        API_BASE_URL: '/api',
    },
    // 生产环境API地址（反向代理不会生效）
    production: {
        API_BASE_URL: 'https://orfm-springboot:9999/api',
    },
};

// 根据当前环境获取配置（npm run serve为development，npm run build为production）
const env = process.env.NODE_ENV || 'development';
const currentConfig = config[env];

// API地址
export const API_BASE_URL = currentConfig.API_BASE_URL;

// 统一的请求超时时间
export const TIMEOUT = 20_000;

// 请求头配置
export const HEADERS = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
};