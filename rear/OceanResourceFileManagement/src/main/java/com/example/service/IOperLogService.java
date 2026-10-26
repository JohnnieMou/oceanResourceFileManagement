package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.entity.OperLog;
import com.example.pojo.vo.OperLogVO;

/**
 * <p>
 * 操作日志记录 服务类
 * </p>
 *
 * @author ccc212
 * @since 2024-10-08
 */
public interface IOperLogService extends IService<OperLog> {

    IPage<OperLogVO> pageQuery(PageDTO pageDTO);

    void clear();
}
