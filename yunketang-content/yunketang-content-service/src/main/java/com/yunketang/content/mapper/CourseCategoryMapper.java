package com.yunketang.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunketang.content.model.dto.CourseCategoryTreeDto;
import com.yunketang.content.model.po.CourseCategory;

import java.util.List;

/**
 * 课程分类 Mapper 接口
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
    List<CourseCategoryTreeDto> selectTreeNodes(String rootId);
}
