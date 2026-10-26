package com.example.controller;

import com.example.BaseTest;
import com.example.config.properties.JwtProperties;
import com.example.pojo.dto.table.AddTableDTO;
import com.example.pojo.dto.table.UpdateTableDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Slf4j
@AutoConfigureMockMvc // 启用 MockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 确保 @BeforeAll 和 @AfterAll 可以使用注入的对象
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 确保测试方法按顺序执行
public class TableControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    JwtProperties jwtProperties;

    @Test
    @Order(1)
    public void testCreateTable() throws Exception {
        // 添加成功
        AddTableDTO addTableDTO = new AddTableDTO("测试表10", "测试摘要", List.of("测试标签"));
        mockMvc.perform(post("/table/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token)
                        .content(objectMapper.writeValueAsString(addTableDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("添加成功"));

        // 表名为空
        addTableDTO = new AddTableDTO();
        mockMvc.perform(post("/table/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token)
                        .content(objectMapper.writeValueAsString(addTableDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("表名不能为空"));
    }

    @Test
    @Order(2)
    public void testUploadConfirm() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "excel",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "Test content".getBytes()
        );

        mockMvc.perform(multipart("/table/uploadConfirm/测试表10")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

//    @Test
//    @Order(3)
//    public void testUploadCover() throws Exception {
//        Map<String, LinkedHashMap<String, String>> fieldListMap = new LinkedHashMap<>();
//        LinkedHashMap<String, String> sheetMap = new LinkedHashMap<>();
//        sheetMap.put("航次", "string");
//        sheetMap.put("姓名", "string");
//        sheetMap.put("日期", "date");
//        sheetMap.put("工资", "number");
//        sheetMap.put("年龄", "number");
//        sheetMap.put("性别", "string");
//        fieldListMap.put("模板", sheetMap);
//        fieldListMap.put("Sheet2", sheetMap);
//        fieldListMap.put("Sheet1", sheetMap);
//        mockMvc.perform(post("/table/uploadCover/测试表10")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header(jwtProperties.getTokenName(), token)
//                        .content(objectMapper.writeValueAsString(fieldListMap)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data").value("上传成功"));
//    }
//
//    @Test
//    @Order(4)
//    public void testUploadAppend() throws Exception {
//        mockMvc.perform(post("/table/uploadAppend/测试表10")
//                        .header(jwtProperties.getTokenName(), token))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data").value("上传成功"));
//    }

    @Test
    @Order(5)
    public void testDownload() throws Exception {
        mockMvc.perform(post("/table/download")
                        .param("tableName", "测试表10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    public void testGetTemplate() throws Exception {
        mockMvc.perform(post("/table/getTemplate")
                        .param("tableName", "测试表10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    public void testCheckTable() throws Exception {
        mockMvc.perform(get("/table/check/测试表10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    public void testGetTableInfo() throws Exception {
        mockMvc.perform(get("/table/info")
                        .param("tableName", "测试表10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @Order(8)
    public void testUpdateTable() throws Exception {
        UpdateTableDTO updateTableDTO = new UpdateTableDTO("测试表10", "测试表11", "测试摘要2", List.of("测试标签"));
        mockMvc.perform(put("/table/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token)
                        .content(objectMapper.writeValueAsString(updateTableDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("修改成功"));
    }

    @Test
    @Order(9)
    public void testDeleteTable() throws Exception {
        mockMvc.perform(delete("/table/测试表11")
                        .header(jwtProperties.getTokenName(), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("删除成功"));
    }
}