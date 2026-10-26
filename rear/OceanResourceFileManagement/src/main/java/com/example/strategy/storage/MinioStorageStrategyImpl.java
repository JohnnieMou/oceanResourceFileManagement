package com.example.strategy.storage;

import com.example.config.properties.MinioProperties;
import com.example.enums.FilePathEnum;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.service.IFileMappingService;
import com.example.utils.FileUtil;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("minioStorageStrategyImpl")
@RequiredArgsConstructor
@Slf4j
public class MinioStorageStrategyImpl extends AbstractStorageStrategyImpl {

    private final MinioProperties minioProperties;

    private final MinioClient minioClient;

    private final IFileMappingService fileMappingService;

    @Override
    public String getPresignUrl(String path, String fileName, HttpServletResponse response) {
        // 获取桶名
        String bucketName = minioProperties.getBucketName();
        String presignedUrl = null;
        try {
            if (StringUtils.isBlank(fileName)) {
                throw new BizException(StatusCodeEnum.FILE_NAME_NOT_NULL);
            }

            // 判断桶是否存在
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (isExist) {
                presignedUrl = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(bucketName)
                                .object(path + fileName)
                                .expiry(7, TimeUnit.DAYS) // 七天过期时间
                                .build());
                return presignedUrl;
            } else {
                log.error("获取文件预览url失败，桶{}不存在", bucketName);
                throw new BizException(StatusCodeEnum.GET_PRESIGN_URL_ERROR);
            }
        } catch (Exception e) {
            log.error("获取文件预览url时出现异常: " + e.getMessage());
            throw new BizException(StatusCodeEnum.GET_PRESIGN_URL_ERROR);
        }
    }

    @Override
    public Boolean exists(String filePath) {
        boolean exist = true;
        try {
            // 获取文件元数据，如果文件不存在会抛出异常
            minioClient.statObject(StatObjectArgs.builder().
                    bucket(minioProperties.getBucketName())
                    .object(filePath)
                    .build());
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    @SneakyThrows
    @Override
    public void upload(String path, String encryptedFileName, InputStream inputStream) {
        String bucketName = minioProperties.getBucketName();
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path + encryptedFileName)
                        .stream(inputStream, inputStream.available(), -1)
                        .build());
    }

    @Override
    public void download(String path, String fileName, HttpServletResponse response) {
        try {
            // 获取文件流
            InputStream file = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(path + fileName)
                    .build());

            // 构建文件名
            fileName = fileMappingService.getOriginalFileName(fileName);

            // 设置响应头
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(fileName.substring(fileName.lastIndexOf("/") + 1), StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");

            // 获取输出流
            ServletOutputStream servletOutputStream = response.getOutputStream();
            int len;
            byte[] buffer = new byte[1024];

            // 读取文件流并写入输出流
            while ((len = file.read(buffer)) > 0) {
                servletOutputStream.write(buffer, 0, len);
            }

            // 刷新输出流，确保所有缓冲数据都被写入输出流
            servletOutputStream.flush();

            // 释放资源
            file.close();
            servletOutputStream.close();
        } catch (BizException biz) {
            throw biz;
        } catch (Exception e) {
            log.error("文件: " + fileName + " 下载时出现异常: " + e);
        }
    }

    @Override
    @SneakyThrows
    public MultipartFile get(String path, String encryptedFileName) {
        InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(path + encryptedFileName)
                .build());
        return FileUtil.inputStreamToMultipart(inputStream, encryptedFileName);
    }

    @Override
    @SneakyThrows
    public void delete(String path, String encryptedFileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(path + encryptedFileName)
                    .build());
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.FILE_DELETE_FAILED);
        }
    }

    @Override
    @SneakyThrows
    public void batchDelete(String path, List<String> encryptedFileNames) {
        try {
            // 将文件路径与文件名拼接成完整的对象路径
            List<DeleteObject> objects = encryptedFileNames.stream()
                    .map(fileName -> new DeleteObject(path + fileName))
                    .collect(Collectors.toList());

            // 调用MinIO的批量删除接口
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .objects(objects)
                    .build());

            // 必须遍历迭代器才能删除成功
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("MinIO删除错误 -> bucketName={}, objectName={}, message={}",
                        error.bucketName(), error.objectName(), error.message());
            }
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.BATCH_DELETE_FAILED);
        }
    }
}
