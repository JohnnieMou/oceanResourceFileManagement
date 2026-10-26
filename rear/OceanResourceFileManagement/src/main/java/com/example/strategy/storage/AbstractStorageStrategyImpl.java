package com.example.strategy.storage;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.constant.FileTypeConstant;
import com.example.enums.FilePathEnum;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.pojo.entity.FileMapping;
import com.example.service.IFileMappingService;
import com.example.utils.FileUtil;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Service
public abstract class AbstractStorageStrategyImpl implements StorageStrategy {

    @Autowired
    private IFileMappingService fileMappingService;

    @Override
    public void uploadFile(String path, MultipartFile file, String fileName, Integer fileType) {
        try {
            @Cleanup
            InputStream inputStream = file.getInputStream();
            String encryptedFileName = FileUtil.generateFileName(file.getOriginalFilename());
            if (!exists(path + encryptedFileName)) {
                upload(path, encryptedFileName, inputStream);
                FileMapping beforeFile = fileMappingService.lambdaQuery().eq(FileMapping::getOriginalFileName, fileName).one();
                // 查看映射是否存在，如果不存在添加，存在则修改
                if (beforeFile == null) {
                    fileMappingService.add(fileName, encryptedFileName, path + encryptedFileName, fileType);
                } else {
                    fileMappingService.update(new LambdaUpdateWrapper<FileMapping>()
                            .eq(FileMapping::getOriginalFileName, fileName)
                            .set(FileMapping::getEncryptedFileName, encryptedFileName)
                            .set(FileMapping::getPath, path + encryptedFileName));
                    if (Objects.equals(fileType, FileTypeConstant.ATTACHMENT)) {
                        delete(FilePathEnum.ATTACHMENT.getPath(), beforeFile.getEncryptedFileName());
                    } else {
                        delete(FilePathEnum.EXCEL.getPath(), beforeFile.getEncryptedFileName());
                    }
                }
            } else {
                throw new BizException(StatusCodeEnum.FILE_EXISTS);
            }
        } catch (BizException biz) {
            throw biz;
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.UPLOAD_FAILED);
        }
    }

    @Override
    public void downloadFile(String path, String fileName, HttpServletResponse response) {
        String encryptedFileName;
        try {
            encryptedFileName = fileMappingService.getEncryptedFileName(fileName);
        } catch (BizException biz) {
            throw biz;
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.FILE_NOT_EXISTS);
        }

        try {
            download(path, encryptedFileName, response);
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.DOWNLOAD_FAILED);
        }
    }

    @Override
    public MultipartFile getExcel(String path, String tableCnName) {
        String encryptedFileName;
        try {
            encryptedFileName = fileMappingService.getEncryptedFileName(tableCnName);
        } catch (BizException biz) {
            throw biz;
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }
        return get(path, encryptedFileName);
    }

    @Override
    public void deleteFile(String path, String encryptedFileName) {
        delete(path, encryptedFileName);
        fileMappingService.remove(new LambdaQueryWrapper<FileMapping>().eq(FileMapping::getEncryptedFileName, encryptedFileName));
    }

    @Override
    public void batchDeleteFile(String path, List<String> encryptedFileNames) {
        if (CollUtil.isEmpty(encryptedFileNames)) {
            throw new BizException(StatusCodeEnum.FILE_LIST_EMPTY);
        }
        try {
            // 调用具体存储策略的批量删除方法
            batchDelete(path, encryptedFileNames);
            // 删除文件映射表中的记录
            fileMappingService.remove(new LambdaQueryWrapper<FileMapping>()
                    .in(FileMapping::getEncryptedFileName, encryptedFileNames));
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.BATCH_DELETE_FAILED);
        }
    }

    @Override
    public String getFilePresignUrl(String path, String fileName, HttpServletResponse response) {
        String encryptedFileName;
        try {
            encryptedFileName = fileMappingService.getEncryptedFileName(fileName);
        } catch (BizException biz) {
            throw biz;
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        return getPresignUrl(path, encryptedFileName, response);
    }

    public abstract String getPresignUrl(String path, String fileName, HttpServletResponse response);

    public abstract Boolean exists(String filePath);

    public abstract void upload(String path, String encryptedFileName, InputStream inputStream) throws IOException;

    public abstract void download(String path, String fileName, HttpServletResponse response);

    public abstract MultipartFile get(String path, String encryptedFileName);

    public abstract void delete(String path, String encryptedFileName);

    public abstract void batchDelete(String path, List<String> encryptedFileNames);

}
