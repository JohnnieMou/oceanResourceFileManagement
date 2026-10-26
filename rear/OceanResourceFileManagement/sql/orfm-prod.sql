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
# 角色表
DROP TABLE IF EXISTS t_role;
CREATE TABLE t_role
(
    `id`        bigint      NOT NULL AUTO_INCREMENT COMMENT '角色主键',
    `role_code` int         NOT NULL COMMENT '角色编号（1为管理员，2为普通用户）',
    `role_name` varchar(50) NOT NULL COMMENT '角色名称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='角色表';

INSERT INTO t_role (role_code, role_name)
VALUES (1, '管理员');
INSERT INTO t_role (role_code, role_name)
VALUES (2, '普通用户');

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

# 标签信息表
DROP TABLE IF EXISTS t_tag;
CREATE TABLE t_tag
(
    `id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '标签主键',
    `tag_name` varchar(255) NOT NULL COMMENT '标签名称',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='标签信息表';

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
    `id`                  bigint               NOT NULL AUTO_INCREMENT comment '映射主键',
    `original_file_name`  varchar(255)         NOT NULL comment '原始文件名称',
    `encrypted_file_name` varchar(255)         NOT NULL comment '加密文件名称',
    `path`                varchar(100)         NOT NULL comment '文件路径',
    `status`              tinyint(1) default 1 comment '状态（1为正常，2为正在合并）',
    `file_type`           tinyint(1) default 1 NOT NULL comment '文件类型（1为临时文件，2为excel文件，3为附件）',
    `upload_time`         datetime   DEFAULT CURRENT_TIMESTAMP comment '上传时间',
    `update_time`         datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    primary key (id),
    INDEX idx_original_name (original_file_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='文件映射表';

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