package com.example.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class AttachmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 附件id
     */
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
     * 文件名
     */
    private String fileName;

    /**
     * 文件链接
     */
    private String fileLink;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
