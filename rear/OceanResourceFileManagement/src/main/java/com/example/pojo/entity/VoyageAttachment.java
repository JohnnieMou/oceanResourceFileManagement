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
 * 航次附件表
 * </p>
 *
 * @author ccc212
 * @since 2025-03-20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_voyage_attachment")
public class VoyageAttachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 地点
     */
    private String location;

    /**
     * 航次
     */
    private Integer voyage;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
