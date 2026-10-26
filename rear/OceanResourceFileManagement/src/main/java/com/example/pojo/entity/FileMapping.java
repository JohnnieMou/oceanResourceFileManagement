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
 * 文件映射表
 * </p>
 *
 * @author ccc212
 * @since 2024-12-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_file_mapping")
public class FileMapping implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String originalFileName;

    private String encryptedFileName;

    private String path;

    private Integer status; // 1为正常，2为正在合并

    private Integer fileType; // 1为临时文件，2为excel文件，3为附件

    private LocalDateTime uploadTime;

    private LocalDateTime updateTime;

}
