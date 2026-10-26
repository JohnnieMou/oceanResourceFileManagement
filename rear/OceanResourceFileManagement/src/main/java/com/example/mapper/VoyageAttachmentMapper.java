package com.example.mapper;

import com.example.pojo.entity.VoyageAttachment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.vo.AttachmentVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 航次附件表 Mapper 接口
 * </p>
 *
 * @author ccc212
 * @since 2025-03-20
 */
@Mapper
public interface VoyageAttachmentMapper extends BaseMapper<VoyageAttachment> {

    Page<AttachmentVO> pageQuery();
}
