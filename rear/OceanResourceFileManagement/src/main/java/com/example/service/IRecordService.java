package com.example.service;

import com.example.pojo.dto.record.AddRecordDTO;
import com.example.pojo.dto.record.DeleteRecordDTO;
import com.example.pojo.dto.record.UpdateRecordDTO;

import java.util.LinkedHashMap;

public interface IRecordService {
    void addRecord(AddRecordDTO addRecordDTO);

    void deleteRecord(DeleteRecordDTO deleteRecordDTO);

    void updateRecord(UpdateRecordDTO updateRecordDTO);

    LinkedHashMap<String, Object> getRecordById(String tableName, String sheetName, Long id);
}
