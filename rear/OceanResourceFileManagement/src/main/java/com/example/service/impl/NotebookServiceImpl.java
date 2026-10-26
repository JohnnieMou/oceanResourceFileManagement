package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.constant.JwtClaimsConstant;
import com.example.context.BaseContext;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.NotebookMapper;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.notebook.AddNotebookDTO;
import com.example.pojo.dto.notebook.UpdateNotebookDTO;
import com.example.pojo.entity.Notebook;
import com.example.pojo.vo.NotebookInfoVO;
import com.example.service.INotebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 记事本表 服务实现类
 * </p>
 *
 * @author ccc212
 * @since 2025-01-04
 */
@Service
@RequiredArgsConstructor
public class NotebookServiceImpl extends ServiceImpl<NotebookMapper, Notebook> implements INotebookService {

    private final NotebookMapper notebookMapper;

    @Override
    public void addNotebook(AddNotebookDTO addNotebookDTO) {
        Notebook notebook = BeanUtil.copyProperties(addNotebookDTO, Notebook.class);
        Long userId = BaseContext.getUserId();
        notebook.setCreateUser(userId);
        notebook.setUpdateUser(userId);
        save(notebook);
    }

    @Override
    public void deleteNotebook(Long id) {
        // 检查记事是否存在
        if (notebookMapper.selectOne(new LambdaQueryWrapper<Notebook>().eq(Notebook::getId, id)) == null) {
            throw new BizException(StatusCodeEnum.NOTEBOOK_NOT_EXISTS);
        }
        removeById(id);
    }

    @Override
    public void updateNotebook(UpdateNotebookDTO updateNotebookDTO) {
        Long id = updateNotebookDTO.getId();

        // 检查记事是否存在
        if (notebookMapper.selectOne(new LambdaQueryWrapper<Notebook>().eq(Notebook::getId, id)) == null) {
            throw new BizException(StatusCodeEnum.NOTEBOOK_NOT_EXISTS);
        }

        Notebook notebook = Notebook.builder()
                .id(id)
                .title(updateNotebookDTO.getNewTitle())
                .content(updateNotebookDTO.getNewContent())
                .updateUser(BaseContext.getUserId())
                .build();
        updateById(notebook);
    }

    @Override
    public IPage<NotebookInfoVO> listNotebook(PageDTO pageDTO) {
        Page<Notebook> page = new Page<>(pageDTO.getPage(), pageDTO.getPageSize());
        IPage<NotebookInfoVO> resultPage = notebookMapper.listNotebook(page, pageDTO);
        return resultPage;
    }
}
