const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    // port: 80,
    proxy: {
      '/api': {
        target: 'https://orfm.ccc212.cn', 
        changeOrigin: true,
        pathRewrite:{
          '^/api':''
        }
      },
      
    }
  }
})
