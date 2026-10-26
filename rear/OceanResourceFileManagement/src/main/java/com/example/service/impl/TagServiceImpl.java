package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.TagMapper;
import com.example.pojo.entity.Tag;
import com.example.service.ITagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2024-09-25
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    private final TagMapper tagMapper;

    @Override
    @Transactional
    public void addTag(String tagName) {
        if (tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getTagName, tagName)) != null) {
            throw new BizException(StatusCodeEnum.TAG_ALREADY_EXISTS);
        }
        tagMapper.insert(Tag.builder()
                .tagName(tagName)
                .build());
    }

    @Override
    @Transactional
    public void deleteTag(String tagName) {
        if (tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getTagName, tagName)) == null) {
            throw new BizException(StatusCodeEnum.TAG_NOT_EXISTS);
        }
        tagMapper.delete(new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tagName));
    }

    @Override
    @Transactional
    public void updateTag(String oldTagName, String newTagName) {
        if (tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getTagName, oldTagName)) == null) {
            throw new BizException(StatusCodeEnum.TAG_NOT_EXISTS);
        }
        tagMapper.update(new LambdaUpdateWrapper<Tag>()
                .eq(Tag::getTagName, oldTagName)
                .set(Tag::getTagName, newTagName));
    }
}
