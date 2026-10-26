package com.example.utils;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

public class FileUtil {

    public static String generateFileName(String originalFileName) {
        String extension = FilenameUtils.getExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + (StringUtils.isNotBlank(extension) ? "." + extension : "");
    }

    public static File multipartFileToFile(MultipartFile multipartFile) {
        File file = null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String[] filename = Objects.requireNonNull(originalFilename).split("\\.");
            file = File.createTempFile(filename[0], filename[1]);
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return file;
    }

    public static MultipartFile inputStreamToMultipart(InputStream inputStream, String fileName) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            return new MockMultipartFile(fileName, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
