package com.example.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DataPermissionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 表中文名称
     */
    private String tableCnName;

    /**
     * 开始年份
     */
    private Integer yearStart;

    /**
     * 结束年份
     */
    private Integer yearEnd;

    /**
     * 开始航次
     */
    private Integer voyageStart;

    /**
     * 结束航次
     */
    private Integer voyageEnd;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
