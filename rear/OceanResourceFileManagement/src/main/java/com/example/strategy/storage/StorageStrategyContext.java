package com.example.strategy.storage;

import com.example.enums.StorageModeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@Service
public class StorageStrategyContext {

    @Value("${storage.mode}")
    private String storageMode;

    @Autowired
    private Map<String, StorageStrategy> storageStrategyMap;

    public void executeUploadExcelStrategy(String path, MultipartFile file, String fileName, Integer fileType) {
        storageStrategyMap.get(StorageModeEnum.getStrategy(storageMode)).uploadFile(path, file, fileName, fileType);
    }

    public void executeDownloadStrategy(String path, String fileName, HttpServletResponse response) {
        storageStrategyMap.get(StorageModeEnum.getStrategy(storageMode)).downloadFile(path, fileName, response);
    }

    public MultipartFile executeGetExcelStrategy(String path, String tableCnName) {
        return storageStrategyMap.get(StorageModeEnum.getStrategy(storageMode)).getExcel(path, tableCnName);
    }

    public void executeDeleteStrategy(String path, String fileName) {
        storageStrategyMap.get(StorageModeEnum.getStrategy(storageMode)).deleteFile(path, fileName);
    }

    public void executeBatchDeleteStrategy(String path, List<String> encryptedFileNames) {
        storageStrategyMap.get(StorageModeEnum.getStrategy(storageMode)).batchDeleteFile(path, encryptedFileNames);
    }

    public String executeGetPresignUrlStrategy(String path, String fileName, HttpServletResponse response) {
        return storageStrategyMap.get(StorageModeEnum.getStrategy(storageMode)).getFilePresignUrl(path, fileName, response);
    }
}
