package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.dto.table.AddTableDTO;
import com.example.pojo.dto.table.UpdateTableDTO;
import com.example.pojo.entity.Table;
import com.example.pojo.vo.TableInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sb
 * @since 2024-09-06
 */
public interface ITableService extends IService<Table> {

    Map<String, LinkedHashMap<String, String>> uploadConfirm(MultipartFile excel, String tableName);

    void upload(Map<String, LinkedHashMap<String, String>> fieldListMap, String tableName, boolean isCover);

    void download(String tableName);

    void dropTable(String tableName);

    void addTable(AddTableDTO addTableDTO);

    List<TableInfoVO> listTable();

    void updateTable(UpdateTableDTO updateTableDTO);

    TableInfoVO getTableInfo(String tableName);

    void getTemplate(String tableName);
}
