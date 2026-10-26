package com.example.strategy.storage;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface StorageStrategy {

    void uploadFile(String path, MultipartFile file, String fileName, Integer fileType);

    void downloadFile(String path, String fileName, HttpServletResponse response);

    MultipartFile getExcel(String path, String tableCnName);

    void deleteFile(String path, String fileName);

    void batchDeleteFile(String path, List<String> encryptedFileNames);


    String getFilePresignUrl(String path, String fileName, HttpServletResponse response);
}
