CREATE DATABASE IF NOT EXISTS orfm;
USE orfm;

# 用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user
(
    `id`               bigint                                          NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    `name`             varchar(50)                                     NOT NULL COMMENT '用户姓名',
    `username`         varchar(50)                                     NOT NULL COMMENT '用户名',
    `password`         varchar(50)                                     NOT NULL COMMENT '密码',
    `role_code`        bigint DEFAULT 3                                NOT NULL COMMENT '角色编号',
    `valid_start_time` date   DEFAULT (CURRENT_DATE)                   NOT NULL COMMENT '有效期开始时间',
    `valid_end_time`   date   DEFAULT (CURRENT_DATE + INTERVAL 1 YEAR) NOT NULL COMMENT '有效期结束时间',
    `create_time`      datetime                                        NOT NULL COMMENT '创建时间',
    `last_login_time`  datetime                                        NOT NULL COMMENT '上次登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='用户信息表';

# 管理员
INSERT INTO t_user (name, username, password, role_code, valid_start_time, valid_end_time, create_time,
                    last_login_time)
VALUES ('管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', 1, '2000-01-01', '3000-01-01', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);
# 过期用户
INSERT INTO t_user (name, username, password, role_code, valid_start_time, valid_end_time, create_time, last_login_time)
VALUES ('过期用户', 'expired', 'e10adc3949ba59abbe56e057f20f883e', 2, '2000-01-01', '2000-01-01', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

# 角色表
DROP TABLE IF EXISTS t_role;
CREATE TABLE t_role
(
    `id`        bigint      NOT NULL AUTO_INCREMENT COMMENT '角色主键',
    `role_code` int         NOT NULL COMMENT '角色编号（1为管理员，2为普通用户，3为游客）',
    `role_name` varchar(50) NOT NULL COMMENT '角色名称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='角色表';

INSERT INTO t_role (role_code, role_name)
VALUES (1, '管理员');
INSERT INTO t_role (role_code, role_name)
VALUES (2, '普通用户');
INSERT INTO t_role (role_code, role_name)
VALUES (3, '游客');

# 数据权限表
DROP TABLE IF EXISTS t_data_permission;
CREATE TABLE t_data_permission
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '权限主键',
    `user_id`       bigint       NOT NULL COMMENT '用户id',
    `table_cn_name` varchar(255) NOT NULL COMMENT '表中文名称',
    `year_start`    int COMMENT '开始年份',
    `year_end`      int COMMENT '结束年份',
    `voyage_start`  int COMMENT '开始航次',
    `voyage_end`    int COMMENT '结束航次',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='数据权限表';

# 记事本表
DROP TABLE IF EXISTS t_notebook;
CREATE TABLE t_notebook
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '记事本主键',
    `title`       varchar(255) NOT NULL COMMENT '记事标题',
    `content`     Text         NOT NULL COMMENT '记事内容',
    `update_user` bigint       NOT NULL COMMENT '修改用户id',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user` bigint       NOT NULL COMMENT '创建用户id',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='记事本表';

# 表信息表
DROP TABLE IF EXISTS t_table;
CREATE TABLE t_table
(
    `id`            bigint                 NOT NULL AUTO_INCREMENT COMMENT '表主键',
    `table_en_name` varchar(255)           NOT NULL COMMENT '表英文名称',
    `table_cn_name` varchar(255)           NOT NULL COMMENT '表中文名称',
    `description`   varchar(255) DEFAULT '' COMMENT '摘要',
    `is_guest`      tinyint(1)   DEFAULT 1 NOT NULL COMMENT '是否是游客的表（1 是，0 否）',
    `create_user`   bigint       DEFAULT 1 NOT NULL COMMENT '创建用户id',
    `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='表信息表';

INSERT INTO `t_table` VALUES (1,'example_table','示例表','摘要',0,1,'2025-02-18 00:11:09');
INSERT INTO `t_table` VALUES (2,'example_table_2','示例表2','摘要',0,1,'2025-02-19 00:11:09');

# sheet信息表
DROP TABLE IF EXISTS t_sheet;
CREATE TABLE t_sheet
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT 'Sheet 主键',
    `table_en_name` varchar(255) NOT NULL COMMENT '表英文名称',
    `sheet_en_name` varchar(255) NOT NULL COMMENT 'Sheet 英文名称',
    `sheet_cn_name` varchar(255) NOT NULL COMMENT 'Sheet 中文名称',
    `contains_geo`  tinyint(1)   NOT NULL COMMENT '是否包含地理信息（1 是，0 否）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='Sheet 信息表';

INSERT INTO `t_sheet` VALUES (1,'example_table','sheet3','sheet3',0);
INSERT INTO `t_sheet` VALUES (2,'example_table_2','sheet3','sheet3',0);

# 字段信息表
DROP TABLE IF EXISTS t_field;
CREATE TABLE t_field
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '字段主键',
    `table_en_name` varchar(255) NOT NULL COMMENT '表英文名称',
    `sheet_en_name` varchar(255) NOT NULL COMMENT 'Sheet 英文名称',
    `field_en_name` varchar(255) NOT NULL COMMENT '字段英文名称',
    `field_cn_name` varchar(255) NOT NULL COMMENT '字段中文名称',
    `data_type`     varchar(50)  NOT NULL COMMENT '数据类型',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='字段信息表';

INSERT INTO `t_field` VALUES (1,'example_table','sheet3','field_0','航次','string'),(2,'example_table','sheet3','field_1','姓名','string'),(3,'example_table','sheet3','field_2','日期','string'),(4,'example_table','sheet3','field_3','工资','string'),(5,'example_table','sheet3','field_4','年龄','number'),(6,'example_table','sheet3','field_5','性别','string'),(7,'example_table','sheet3','field_6','新增字段','string');
INSERT INTO `t_field` VALUES (8,'example_table_2','sheet3','field_0','航次','string'),(9,'example_table_2','sheet3','field_1','姓名','string'),(10,'example_table_2','sheet3','field_2','日期','string'),(11,'example_table_2','sheet3','field_3','工资','string'),(12,'example_table_2','sheet3','field_4','年龄','number'),(13,'example_table_2','sheet3','field_5','性别','string'),(14,'example_table_2','sheet3', 'field_6','新增字段','string');

# 标签信息表
DROP TABLE IF EXISTS t_tag;
CREATE TABLE t_tag
(
    `id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '标签主键',
    `tag_name` varchar(255) NOT NULL COMMENT '标签名称',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='标签信息表';

INSERT INTO t_tag (tag_name)
VALUES ('测试标签');

# 表和标签的关联表
DROP TABLE IF EXISTS t_table_tag;
CREATE TABLE t_table_tag
(
    `table_id` bigint NOT NULL COMMENT '表主键',
    `tag_id`   bigint NOT NULL COMMENT '标签主键',
    PRIMARY KEY (`table_id`, `tag_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='表与标签的关联表';

# 操作日志表
DROP TABLE IF EXISTS t_oper_log;
CREATE TABLE t_oper_log
(
    `id`             bigint     NOT NULL AUTO_INCREMENT COMMENT '日志主键',
    `title`          varchar(50)         DEFAULT '' COMMENT '模块标题',
    `business_type`  varchar(20)         DEFAULT 'OTHER' COMMENT '业务类型',
    `method`         varchar(100)        DEFAULT '' COMMENT '方法名称',
    `request_method` varchar(10)         DEFAULT '' COMMENT '请求方式',
    `oper_name`      varchar(50)         DEFAULT '' COMMENT '操作人员',
    `oper_url`       varchar(255)        DEFAULT '' COMMENT '请求URL',
    `oper_ip`        varchar(128)        DEFAULT '' COMMENT '主机地址',
    `oper_param`     varchar(2000)       DEFAULT '' COMMENT '请求参数',
    `json_result`    varchar(2000)       DEFAULT '' COMMENT '返回参数',
    `status`         int(1)              DEFAULT '0' COMMENT '操作状态（1正常 0异常）',
    `error_msg`      varchar(2000)       DEFAULT '' COMMENT '错误消息',
    `oper_time`      datetime            DEFAULT NULL COMMENT '操作时间',
    `execute_time`   bigint(20) NOT NULL DEFAULT '0' COMMENT '执行时长(毫秒)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='操作日志记录';

# 文件映射表
DROP TABLE IF EXISTS t_file_mapping;
CREATE TABLE t_file_mapping
(
    `id`                  bigint       NOT NULL AUTO_INCREMENT comment '映射主键',
    `original_file_name`  varchar(255) NOT NULL comment '原始文件名称',
    `encrypted_file_name` varchar(255) NOT NULL comment '加密文件名称',
    `path`                varchar(100) NOT NULL comment '文件路径',
    `status`              tinyint(1) default 1 comment '状态（1为正常，2为正在合并）',
    `file_type`           tinyint(1) default 1  NOT NULL comment '文件类型（1为临时文件，2为excel文件，3为附件）',
    `upload_time`         datetime   DEFAULT CURRENT_TIMESTAMP comment '上传时间',
    `update_time`         datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    primary key (id),
    INDEX idx_original_name (original_file_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='文件映射表';

INSERT INTO `t_file_mapping` VALUES (1,'示例表','16da2c8bf03045dd890a7538abad9bd2.xlsx','excel/16da2c8bf03045dd890a7538abad9bd2.xlsx',1,2,'2025-02-18 00:16:10','2025-02-18 00:16:10');
INSERT INTO `t_file_mapping` VALUES (2,'示例表2','16da2c8bf03045dd890a7538abad9bd2.xlsx','excel/16da2c8bf03045dd890a7538abad9bd2.xlsx',1,2,'2025-02-18 00:16:10','2025-02-18 00:16:10');

DROP TABLE IF EXISTS `example_table___sheet3`;
CREATE TABLE `example_table___sheet3` (
                                          `id` bigint NOT NULL AUTO_INCREMENT,
                                          `field_0` text,
                                          `field_1` text,
                                          `field_2` text,
                                          `field_3` text,
                                          `field_4` decimal(10,0) DEFAULT NULL,
                                          `field_5` text,
                                          `field_6` text,
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `example_table___sheet3` VALUES (1,'1','郑八','2024/07/22','14',35,'女',NULL),(2,'1','褚十','2024/07/22','2',22,'男',NULL),(3,'1','冯二','2024/07/22','24',34,'男',NULL),(4,'1','吴三','2024/07/22','25',32,'男',NULL),(5,'1','郑五','2024/07/22','26',29,'女',NULL),(6,'1','褚四','2024/07/22','8',50,'女',NULL),(7,'1','冯三','2024/07/22','13',18,'女',NULL),(8,'1','李三','2024/07/22','19',24,'男',NULL),(9,'1','周六','2024/07/22','30',44,'女',NULL),(10,'1','李一','2024/07/22','23',38,'男',NULL),(11,'1','郑六','2024/07/22','22',38,'女',NULL),(12,'1','冯四','2024/07/22','6',35,'男',NULL),(13,'1','赵一','2024/07/22','20',42,'女',NULL),(14,'1','赵三','2024/07/22','23',58,'男',NULL),(15,'1','周七','2024/07/22','8',50,'女',NULL),(16,'1','吴三','2024/07/22','30',44,'男',NULL),(17,'1','李九','2024/07/22','16',31,'女',NULL),(18,'1','陈一','2024/07/22','17',57,'男',NULL),(19,'1','郑二','2024/07/22','20',52,'女',NULL),(20,'1','陈五','2024-7-22','14',30,'男',NULL),(21,'1','sheet3','2024-12-28','50',20,'男','无');

DROP TABLE IF EXISTS `example_table_2___sheet3`;
CREATE TABLE `example_table_2___sheet3` (
                                          `id` bigint NOT NULL AUTO_INCREMENT,
                                          `field_0` text,
                                          `field_1` text,
                                          `field_2` text,
                                          `field_3` text,
                                          `field_4` decimal(10,0) DEFAULT NULL,
                                          `field_5` text,
                                          `field_6` text,
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
INSERT INTO `example_table_2___sheet3` VALUES (1,'1','郑八','2024/07/22','14',35,'女',NULL),(2,'1','褚十','2024/07/22','2',22,'男',NULL),(3,'1','冯二','2024/07/22','24',34,'男',NULL),(4,'1','吴三','2024/07/22','25',32,'男',NULL),(5,'1','郑五','2024/07/22','26',29,'女',NULL),(6,'1','褚四','2024/07/22','8',50,'女',NULL),(7,'1','冯三','2024/07/22','13',18,'女',NULL),(8,'1','李三','2024/07/22','19',24,'男',NULL),(9,'1','周六','2024/07/22','30',44,'女',NULL),(10,'1','李一','2024/07/22','23',38,'男',NULL),(11,'1','郑六','2024/07/22','22',38,'女',NULL),(12,'1','冯四','2024/07/22','6',35,'男',NULL),(13,'1','赵一','2024/07/22','20',42,'女',NULL),(14,'1','赵三','2024/07/22','23',58,'男',NULL),(15,'1','周七','2024/07/22','8',50,'女',NULL),(16,'1','吴三','2024/07/22','30',44,'男',NULL),(17,'1','李九','2024/07/22','16',31,'女',NULL),(18,'1','陈一','2024/07/22','17',57,'男',NULL),(19,'1','郑二','2024/07/22','20',52,'女',NULL),(20,'1','陈五','2024-7-22','14',30,'男',NULL),(21,'1','sheet3','2024-12-28','50',20,'男','无');

# 航次附件表
DROP TABLE IF EXISTS t_voyage_attachment;
CREATE TABLE t_voyage_attachment
(
    `id`            bigint       NOT NULL AUTO_INCREMENT comment '主键',
    `location`      varchar(255) NOT NULL comment '地点',
    `voyage`        int          NOT NULL comment '航次',
    `file_id`       bigint       NOT NULL comment '文件id',
    `upload_time`   datetime DEFAULT CURRENT_TIMESTAMP comment '上传时间',
    `update_time`   datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    primary key (id),
    INDEX idx_voyage_id (voyage)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='航次附件表';