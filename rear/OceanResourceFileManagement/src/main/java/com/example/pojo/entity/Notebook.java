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
 * 记事本表
 * </p>
 *
 * @author ccc212
 * @since 2025-01-04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_notebook")
public class Notebook implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记事本主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 记事标题
     */
    private String title;

    /**
     * 记事内容
     */
    private String content;

    /**
     * 修改用户id
     */
    private Long updateUser;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户id
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
