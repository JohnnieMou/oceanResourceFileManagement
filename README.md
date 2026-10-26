# 海洋资源管理系统-用户手册

## 约定
1. 传入的Excel表中不能有没有列名的数据。
2. 形如 `<3` 的数值，改用 `-3` 表示。
3. 日期只有年月的，默认为1号，格式统一为 `2000.01.01`。
4. 数据中存在的备注信息，将被放到最右边生成的备注列中。
5. 经纬度数据，将自动转换为十进制。
6. 新注册的用户有效期为 `1` 年，且为游客，只能查看自己添加的表；经过管理员的操作才能查看到其他表
7. 航次在第一列

## 项目背景
海洋资源管理系统旨在提供一个高效的管理平台，用于管理海洋资源相关的数据和信息。系统支持用户管理、文件管理、日志记录等功能，支持任何格式的Excel文件的导入和导出。

## 功能
1. 表管理
2. 记事本
3. 标签管理
4. 日志记录
5. 用户管理

## 功能描述

### 表管理
- 支持创建、修改、删除和查询表信息。

### 记事本
- 支持创建、编辑和删除记事。
- 记录用户的日常操作。

### 标签管理
- 支持为表添加和管理标签。
- 提供标签的增删改查功能。

### 日志记录
- 自动记录用户的操作日志。
- 支持日志的查询。

### 用户管理
- 支持用户的注册、登录和权限管理。
- 提供角色管理功能，支持不同角色的权限控制。

## 技术栈
- **后端**：Spring Boot, MyBatis Plus, Redis, RabbitMQ, Minio
- **数据库**：MySQL
- **API文档**：Swagger

## 安装步骤
1. 克隆项目到本地：
   ```bash
   git clone https://github.com/ccc212/oceanResourceFileManagement.git
   ```
2. 进入项目目录：
   ```bash
   cd oceanResourceFileManagement/rear/OceanResourceFileManagement-mp
   ```
3. 配置数据库：
   - 在`src/main/resources/application.yml`中配置数据库连接信息。
   - 执行`sql/orfm.sql`初始化数据库。

4. 启动项目：
   ```bash
   # 构建项目
   mvn clean install -DskipTests    # 跳过测试以加快构建
   
   # 运行项目
   mvn spring-boot:run

   # 或者直接运行生成的jar文件
   java -jar target/oceanResourceFileManagement.jar
   ```

   常见问题：
   - 如果遇到构建失败，请确保：
     - Maven已正确安装（运行 `mvn -v` 检查）
     - JDK版本 >= 17
     - 所有依赖都能正常下载
   - 如果启动失败，请检查：
     - 配置文件中的端口是否被占用
     - 数据库、Redis等服务是否正常运行

## API文档
- 启动项目后，通过http://localhost:9999/doc.html访问API文档
