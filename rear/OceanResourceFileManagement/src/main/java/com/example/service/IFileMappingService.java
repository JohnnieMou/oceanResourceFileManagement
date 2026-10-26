package com.example.service;

import com.example.pojo.entity.FileMapping;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 文件映射表 服务类
 * </p>
 *
 * @author ccc212
 * @since 2024-12-26
 */
public interface IFileMappingService extends IService<FileMapping> {

    void add(String originalFileName, String encryptedFileName, String path, Integer fileType);

    String getEncryptedFileName(String originalFileName);

    String getOriginalFileName(String encryptedFileName);

    Long getFileId(String originalFileName);
}
