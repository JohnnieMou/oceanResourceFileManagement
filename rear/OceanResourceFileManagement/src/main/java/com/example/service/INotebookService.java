package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.notebook.AddNotebookDTO;
import com.example.pojo.dto.notebook.UpdateNotebookDTO;
import com.example.pojo.entity.Notebook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.vo.NotebookInfoVO;

/**
 * <p>
 * 记事本表 服务类
 * </p>
 *
 * @author ccc212
 * @since 2025-01-04
 */
public interface INotebookService extends IService<Notebook> {

    void addNotebook(AddNotebookDTO addNotebookDTO);

    void deleteNotebook(Long id);

    void updateNotebook(UpdateNotebookDTO updateNotebookDTO);

    IPage<NotebookInfoVO> listNotebook(PageDTO pageDTO);
}
