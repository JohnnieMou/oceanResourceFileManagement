package com.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCodeEnum {

    // 通用状态码
    SUCCESS(0, "操作成功"),
    FAIL(-10001, "操作失败"),
    SERVER_ERROR(-10002, "服务器内部错误"),
    PARAM_ERROR(-10003, "参数校验失败"),
    NOT_FOUND(-10004, "未找到"),
    NO_PERMISSION(-10005, "权限不足"),

    // 登录相关
    NO_LOGIN(-10101, "用户未登录"),
    PASSWORD_ERROR(-10102, "密码错误"), 
    USERNAME_EXIST(-10103, "用户名已存在"),
    TOKEN_EXPIRED(-10104, "Token已过期"),
    TOKEN_INVALID(-10105, "Token验证失败"),
    ACCOUNT_NOT_FOUND(-10106, "账号不存在"),
    LOGIN_FAILED(-10107, "登录失败"),
    USER_NOT_FOUND(-10108, "用户不存在"),
    USER_EXPIRED(-10109, "用户已过期"),

    // 表相关
    TABLE_NAME_NOT_NULL(-10201, "表名不能为空"),
    TABLE_NAME_TOO_LONG(-10202, "表名太长"),
    TABLE_ALREADY_EXISTS(-10203, "表已存在"),
    TABLE_NOT_EXISTS(-10204, "表不存在"),
    TABLE_NOT_CREATED(-10205, "请先创建表"),
    TABLE_OR_SHEET_NOT_EXISTS_COMBINATION(-10206, "表和Sheet的组合不存在"),
    TABLE_HAS_NO_SHEET(-10207, "该表不包含任何sheet，请上传sheet或删除该表"),

    // Sheet相关
    SHEET_NAME_NOT_NULL(-10301, "Sheet名不能为空"),
    SHEET_NOT_EXISTS(-10302, "Sheet不存在"),
    SHEET_NAME_REPEATED_WHEN_APPEND(-10303, "Sheet名与已有重复（请修改sheet名重新上传进行追加）"),
    RESULT_NOT_EXISTS(-10304, "条件查询结果不存在"),
    VOYAGE_NOT_EXISTS(-10305, "航次不存在"),

    // 字段相关
    FIELD_NAME_NOT_NULL(-10401, "字段名不能为空"),
    FIELD_NOT_EXISTS(-10402, "字段不存在"),
    NO_NUMBER_FIELD(-10403, "没有数值字段"),
    FIELD_ALREADY_EXISTS(-10404, "字段已存在"),
    DATA_TYPE_NOT_MATCH(-10405, "数据与字段类型不匹配"),
    FIELD_NAME_IS_NULL_OR_FIELD_NOT_EXISTS(-10406, "字段名为空或字段不存在"),

    // 标签相关
    TAG_ALREADY_EXISTS(-10501, "标签已存在"),
    TAG_NOT_EXISTS(-10502, "标签不存在"),

    // 文件相关
    UPLOAD_FAILED(-10601, "文件上传失败"),
    DOWNLOAD_FAILED(-10602, "文件下载失败"),
    FILE_NOT_EXISTS(-10603, "文件不存在"),
    FILE_EXISTS(-10604, "文件已存在"),
    PLEASE_OVERWRITE_UPLOAD(-10605, "请先覆盖上传"),
    FILE_MERGING(-10606, "文件正在合并中"),
    FILE_DELETE_FAILED(-10607, "文件删除失败"),
    FILE_LIST_EMPTY(-10608,"文件列表为空"),
    BATCH_DELETE_FAILED(-10609, "批量删除文件失败"),
    FILE_NAME_NOT_NULL(-10610, "文件名不能为空"),
    GET_PRESIGN_URL_ERROR(-10611,  "获取文件URL失败"),

    // 角色相关
    ROLE_NOT_EXISTS(-10701, "角色不存在"),
    NEED_ADMIN(-10702, "需管理员权限"),

    // 记事本相关
    TITLE_ALREADY_EXISTS(-10801, "标题已存在"),
    TITLE_NOT_EXISTS(-10802, "标题不存在"),
    NOTEBOOK_NOT_EXISTS(-10803, "记事不存在"),

    // 数据权限相关
    YEAR_START_GREATER_THAN_YEAR_END(-10901, "开始年份不能大于结束年份"),
    VOYAGE_START_GREATER_THAN_VOYAGE_END(-10902, "开始航次不能大于结束航次"),

    // 其他
    TRANSLATE_PORT_OCCUPIED(-90001, "翻译端口被占用"),
    TOO_MANY_REQUESTS(-90002, "访问过于频繁");


    private final Integer code;

    private final String desc;
}
