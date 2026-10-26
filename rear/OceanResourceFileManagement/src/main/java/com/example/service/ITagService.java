package com.example.service;

import com.example.pojo.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ccc212
 * @since 2024-09-25
 */
public interface ITagService extends IService<Tag> {

    void addTag(String tagName);

    void deleteTag(String tagName);

    void updateTag(String oldTagName, String newTagName);
}
