package com.example.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class TableInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String tableName;

    private String description;

    private List<String> tags;

}
