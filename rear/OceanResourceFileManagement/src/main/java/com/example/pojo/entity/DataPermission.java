package com.example.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 数据权限表
 * </p>
 *
 * @author ccc212
 * @since 2025-01-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_data_permission")
public class DataPermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

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
