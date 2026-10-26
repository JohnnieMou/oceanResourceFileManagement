package com.example.service.impl;

import cn.hutool.core.io.resource.ClassPathResource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.BaseTest;
import com.example.config.properties.CacheProperties;
import com.example.config.properties.JwtProperties;
import com.example.config.properties.LogProperties;
import com.example.exception.BizException;
import com.example.manager.LockManager;
import com.example.mapper.*;
import com.example.pojo.dto.table.AddTableDTO;
import com.example.pojo.dto.table.UpdateTableDTO;
import com.example.pojo.entity.Table;
import com.example.service.IFileMappingService;
import com.example.service.IUserService;
import com.example.strategy.storage.StorageStrategyContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 确保 @BeforeAll 和 @AfterAll 可以使用注入的对象
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 确保测试方法按顺序执行
public class TableServiceImplTest extends BaseTest {

    @Autowired
    DynamicMapper dynamicMapper;
    @Autowired
    TableMapper tableMapper;
    @Autowired
    SheetMapper sheetMapper;
    @Autowired
    FieldMapper fieldMapper;
    @Autowired
    TagMapper tagMapper;
    @Autowired
    TableTagMapper tableTagMapper;
    @Autowired
    LogProperties logProperties;
    @Autowired
    HttpServletResponse response;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    CacheProperties cacheProperties;
    @Autowired
    StorageStrategyContext storageStrategyContext;
    @Autowired
    IFileMappingService fileMappingService;
    @Autowired
    LockManager lockManager;
    @Autowired
    UserMapper userMapper;
    @Autowired
    IUserService userService;
    @Autowired
    TableServiceImpl tableServiceImpl;
    @Autowired
    UserServiceImpl userServiceImpl;
    @Autowired
    JwtProperties jwtProperties;

    @Test
    @Order(1)
    public void testAddTable() throws Exception {
        // 添加成功
        Assertions.assertDoesNotThrow(() -> {
            tableServiceImpl.addTable(AddTableDTO.builder()
                    .tableName("测试表")
                    .description("测试摘要")
                    .tags(List.of("测试标签"))
                    .build());
        });

        // 表已存在
        BizException exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.addTable(AddTableDTO.builder()
                    .tableName("测试表")
                    .description("测试摘要")
                    .tags(List.of("测试标签"))
                    .build());
        });
        Assertions.assertEquals(exception.getMessage(), "表已存在");

        // 标签不存在
        exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.addTable(AddTableDTO.builder()
                    .tableName("不存在标签的表")
                    .tags(List.of("不存在的标签"))
                    .build());
        });
        Assertions.assertEquals(exception.getMessage(), "标签不存在");

    }

    @Test
    @Order(2)
    public void testGetTableInfo() throws Exception {
        // 获取成功
        Assertions.assertDoesNotThrow(() -> {
            tableServiceImpl.getTableInfo("测试表");
        });

        // 表不存在
        BizException exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.getTableInfo("不存在的表");
        });
        Assertions.assertEquals(exception.getMessage(), "表不存在");
    }

    @Test
    @Order(3)
    public void testUploadConfirm() throws Exception {
        // 上传成功
        ClassPathResource resource = new ClassPathResource("test.xlsx");
        InputStream inputStream = resource.getStream();
        byte[] excelContent = StreamUtils.copyToByteArray(inputStream);
        MultipartFile file = new MockMultipartFile(
                "excel",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelContent
        );
        Assertions.assertDoesNotThrow(() -> {
            tableServiceImpl.uploadConfirm(file, "测试表");
        });

        // 表不存在
        BizException exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.uploadConfirm(file, "不存在的表");
        });
        Assertions.assertEquals(exception.getMessage(), "表不存在");
    }

    @Test
    @Order(4)
    public void testUpload() throws Exception {
        // 覆盖上传成功
        Map<String, LinkedHashMap<String, String>> fieldListMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> sheetMap = new LinkedHashMap<>();
        sheetMap.put("航次", "string");
        sheetMap.put("姓名", "string");
        sheetMap.put("日期", "date");
        sheetMap.put("工资", "number");
        sheetMap.put("年龄", "number");
        sheetMap.put("性别", "string");
        fieldListMap.put("模板", sheetMap);
        fieldListMap.put("Sheet2", sheetMap);
        fieldListMap.put("Sheet1", sheetMap);
        Assertions.assertDoesNotThrow(() -> {
            tableServiceImpl.upload(fieldListMap, "测试表", true);
        });

        // 追加上传成功
        Assertions.assertDoesNotThrow(() -> {
            tableServiceImpl.upload(fieldListMap, "测试表", false);
        });

        // 表不存在
        BizException exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.upload(fieldListMap, "不存在的表", true);
        });
        Assertions.assertEquals(exception.getMessage(), "表不存在");
    }

    @Test
    @Order(5)
    public void testListTable() throws Exception {
        // 获取成功
        Assertions.assertDoesNotThrow(() -> {
            tableServiceImpl.listTable();
        });
    }

    @Test
    @Order(6)
    public void testDownload() throws Exception {
        // 下载成功
        Assertions.assertDoesNotThrow(() -> {
            tableServiceImpl.download("测试表");
        });

        // 表不存在
        BizException exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.download("不存在的表");
        });
        Assertions.assertEquals(exception.getMessage(), "表不存在");
    }

    @Test
    @Order(7)
    public void testGetTemplate() throws Exception {
        // 下载成功
        Assertions.assertDoesNotThrow(() -> {
            tableServiceImpl.getTemplate("测试表");
        });

        // 表不存在
        BizException exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.getTemplate("不存在的表");
        });
        Assertions.assertEquals(exception.getMessage(), "表不存在");
    }

    @Test
    @Order(8)
    public void testUpdateTable() throws Exception {
        // 修改成功
        Assertions.assertDoesNotThrow(() -> {
            tableServiceImpl.updateTable(UpdateTableDTO.builder()
                    .oldTableName("测试表")
                    .newTableName("测试表1")
                    .description("测试摘要2")
                    .build());
        });

        // 表不存在
        BizException exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.updateTable(UpdateTableDTO.builder()
                    .oldTableName("不存在的表")
                    .newTableName("测试表1")
                    .description("测试摘要2")
                    .build());
        });
        Assertions.assertEquals(exception.getMessage(), "表不存在");

        // 表名已存在
        exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.updateTable(UpdateTableDTO.builder()
                    .oldTableName("测试表1")
                    .newTableName("测试表1")
                    .description("测试摘要2")
                    .build());
        });
        Assertions.assertEquals(exception.getMessage(), "表已存在");
    }

    @Test
    @Order(9)
    public void testDropTable() throws Exception {
        // 删除成功
        Assertions.assertDoesNotThrow(() -> {
            Table table = tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, "测试表1"));
            tableServiceImpl.dropTable(table.getTableEnName());
        });

        // 表不存在
        BizException exception = Assertions.assertThrows(BizException.class, () -> {
            tableServiceImpl.dropTable("不存在的表");
        });
        Assertions.assertEquals(exception.getMessage(), "表不存在");
    }
}