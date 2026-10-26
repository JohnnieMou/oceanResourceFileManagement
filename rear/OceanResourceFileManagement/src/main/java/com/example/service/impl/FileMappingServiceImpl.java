package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.constant.FileStatusConstant;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.FileMappingMapper;
import com.example.pojo.entity.FileMapping;
import com.example.service.IFileMappingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * <p>
 * 文件映射表 服务实现类
 * </p>
 *
 * @author ccc212
 * @since 2024-12-26
 */
@Service
public class FileMappingServiceImpl extends ServiceImpl<FileMappingMapper, FileMapping> implements IFileMappingService {

    @Override
    @Transactional
    public void add(String originalFileName, String encryptedFileName, String path, Integer fileType) {
        if (StringUtils.isAnyBlank(originalFileName, encryptedFileName, path)) {
            throw new BizException(StatusCodeEnum.PARAM_ERROR);
        }
        save(FileMapping.builder()
                .originalFileName(originalFileName)
                .encryptedFileName(encryptedFileName)
                .path(path)
                .fileType(fileType)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public String getEncryptedFileName(String originalFileName) {
        if (StringUtils.isBlank(originalFileName)) {
            throw new BizException(StatusCodeEnum.FILE_NAME_NOT_NULL);
        }

        FileMapping file = lambdaQuery().eq(FileMapping::getOriginalFileName, originalFileName).one();
//        if (Objects.equals(file.getStatus(), FileStatusConstant.MERGING)) {
//            throw new BizException(StatusCodeEnum.FILE_MERGING);
//        }

        return file.getEncryptedFileName();
    }

    @Override
    @Transactional(readOnly = true)
    public String getOriginalFileName(String encryptedFileName) {
        if (StringUtils.isBlank(encryptedFileName)) {
            throw new BizException(StatusCodeEnum.FILE_NAME_NOT_NULL);
        }

        FileMapping file = lambdaQuery().eq(FileMapping::getEncryptedFileName, encryptedFileName).one();
//        if (Objects.equals(file.getStatus(), FileStatusConstant.MERGING)) {
//            throw new BizException(StatusCodeEnum.FILE_MERGING);
//        }

        return file.getOriginalFileName();
    }

    @Override
    public Long getFileId(String originalFileName) {
        return lambdaQuery().eq(FileMapping::getOriginalFileName, originalFileName).one().getId();
    }
}
