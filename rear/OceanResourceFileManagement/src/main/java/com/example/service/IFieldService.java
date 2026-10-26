package com.example.service;

import com.example.pojo.dto.field.AddFieldDTO;
import com.example.pojo.dto.field.DeleteFieldDTO;
import com.example.pojo.dto.field.UpdateFieldDTO;
import com.example.pojo.entity.Field;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.vo.FieldVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2024-09-06
 */
public interface IFieldService extends IService<Field> {

    void addField(AddFieldDTO addFieldDTO);

    void deleteField(DeleteFieldDTO deleteFieldDTO);

    void updateField(UpdateFieldDTO updateFieldDTO);

    List<FieldVO> listField(String tableName, String sheetName);
}
