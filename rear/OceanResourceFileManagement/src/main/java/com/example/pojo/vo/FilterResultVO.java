package com.example.pojo.vo;

import com.example.pojo.entity.PageResult;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FilterResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 查询结果
    private PageResult pageResult;
    
    // 下载key
    private String downloadKey;
} 