package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.sheet.ExcelPageDTO;
import com.example.pojo.dto.sheet.FilterDTO;
import com.example.pojo.vo.FilterResultVO;
import com.example.pojo.entity.PageResult;
import com.example.pojo.entity.Sheet;
import com.example.pojo.vo.ExcelColumnStatisticsVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ccc212
 * @since 2024-09-06
 */
public interface ISheetService extends IService<Sheet> {

    List<String> getSheets(String tableName);

    PageResult pageQuery(ExcelPageDTO excelPageDTO);

    List<ExcelColumnStatisticsVO> statistics(String tableEnName, String sheetEnName);

    FilterResultVO filter(FilterDTO filterDTO);

    void download(String tableName, String sheetName);

    void downloadFilterResult(String tableName, String sheetName, String key);

    Map<String, LinkedHashMap<String, String>> uploadConfirm(MultipartFile excel, String tableName);

    void upload(Map<String, LinkedHashMap<String, String>> fieldListMap, String tableName);

    void getTemplate(String tableName, String sheetName);

    void delete(String tableName, String sheetName);

    void uploadAttachment(MultipartFile file, String location, Integer voyage);

    void deleteAttachment(List<Integer> ids);

    void downloadAttachment(Long id);

    PageResult attachmentPage(PageDTO pageDTO);
}
