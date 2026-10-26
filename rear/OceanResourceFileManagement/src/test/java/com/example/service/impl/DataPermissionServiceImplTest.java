package com.example.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mapper.DataPermissionMapper;
import com.example.mapper.TableMapper;
import com.example.mapper.UserMapper;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.dataPermission.AddDataPermissionDTO;
import com.example.pojo.dto.dataPermission.DataPermissionDTO;
import com.example.pojo.dto.dataPermission.UpdateDataPermissionDTO;
import com.example.pojo.vo.DataPermissionVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 确保 @BeforeAll 和 @AfterAll 可以使用注入的对象
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 确保测试方法按顺序执行
public class DataPermissionServiceImplTest {

    @Autowired
    DataPermissionMapper dataPermissionMapper;
    @Autowired
    TableMapper tableMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    DataPermissionServiceImpl dataPermissionServiceImpl;

    @Test
    @Order(1)
    public void testAddDataPermission() throws Exception {
        // 添加成功
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionServiceImpl.addDataPermission(AddDataPermissionDTO.builder()
                    .userId(1L)
                    .tableNames(List.of("示例表"))
                    .yearStart(2023)
                    .yearEnd(2026)
                    .voyageStart(1)
                    .voyageEnd(202612)
                    .build());
        });
    }

    @Test
    @Order((2))
    public void testUpdateDataPermission() throws Exception {
        // 修改成功
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionServiceImpl.updateDataPermission(UpdateDataPermissionDTO.builder()
                    .userId(1L)
                    .tableNames(List.of("示例表", "示例表2"))
                    .yearStart(2000)
                    .yearEnd(3000)
                    .voyageStart(100000)
                    .voyageEnd(300000)
                    .build());
        });
    }

    @Test
    @Order(3)
    public void testListDataPermission() throws Exception {
        // 查询成功
        Assertions.assertDoesNotThrow(() -> {
            PageDTO pageDTO = new PageDTO();
            pageDTO.setPage(1);
            pageDTO.setPageSize(10);
            IPage<DataPermissionVO> dataPermissionVOIPage = dataPermissionServiceImpl.listDataPermission(pageDTO);
        });
    }

    @Test
    @Order(4)
    public void testDeleteDataPermission() throws Exception {
        // 删除成功
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionServiceImpl.deleteDataPermission(DataPermissionDTO.builder()
                    .userId(1L)
                    .tableNames(List.of("示例表", "示例表2"))
                    .build());
        });
    }

    @Test
    @Order(5)
    public void testListNoPermissionTables() throws Exception {
        // 查询成功
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionServiceImpl.listNoPermissionTables(1L);
        });
    }

    @Test
    @Order(6)
    public void testGetByUserId() {
        // 查询成功
        Assertions.assertDoesNotThrow(() -> {
            dataPermissionServiceImpl.getByUserId(1L);
        });
    }



}