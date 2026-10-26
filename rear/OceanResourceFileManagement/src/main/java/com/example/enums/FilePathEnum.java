package com.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilePathEnum {

    EXCEL("excel/", "Excel文件路径", ".xlsx"),

    ATTACHMENT("attachment/", "附件路径", ".jpg|.png|.pdf");

    private final String path;

    private final String desc;

    private final String ext;

}
