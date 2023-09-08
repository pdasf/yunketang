package com.yunketang.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunketang.content.model.dto.TeachplanDto;
import com.yunketang.content.model.po.Teachplan;

import java.util.List;

/**
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author cyborg2077
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    List<TeachplanDto> selectTreeNodes(Long courseId);
}
