package com.example.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class NotebookInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 记事id
    private Long id;

    // 标题
    private String title;

    // 记事内容
    private String content;

    // 修改用户
    private String updateUser;

    // 修改时间
    private LocalDateTime updateTime;

    // 创建用户
    private String createUser;

    // 创建时间
    private LocalDateTime createTime;
}
