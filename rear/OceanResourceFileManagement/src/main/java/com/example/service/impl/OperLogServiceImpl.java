package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mapper.OperLogMapper;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.entity.OperLog;
import com.example.pojo.vo.OperLogVO;
import com.example.service.IOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 操作日志记录 服务实现类
 * </p>
 *
 * @author ccc212
 * @since 2024-10-08
 */
@Service
@RequiredArgsConstructor
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements IOperLogService {

    private final OperLogMapper operLogMapper;

    @Override
    @Transactional(readOnly = true)
    public IPage<OperLogVO> pageQuery(PageDTO pageDTO) {
        Page<OperLog> page = new Page<>(pageDTO.getPage(), pageDTO.getPageSize());
        IPage<OperLogVO> resultPage = operLogMapper.pageQuery(page, pageDTO);
        return resultPage;
    }

    @Override
    @Transactional
    public void clear() {
        operLogMapper.delete(new LambdaQueryWrapper<>());
    }
}
