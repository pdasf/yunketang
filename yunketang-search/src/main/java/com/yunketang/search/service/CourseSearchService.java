package com.yunketang.search.service;

import com.yunketang.base.model.PageParams;
import com.yunketang.search.dto.SearchCourseParamDto;
import com.yunketang.search.dto.SearchPageResultDto;
import com.yunketang.search.po.CourseIndex;

/**
 * 课程搜索service
 */
public interface CourseSearchService {
    /**
     * 搜索课程列表
     *
     * @param pageParams           分页参数
     * @param searchCourseParamDto 搜索条件
     */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);
}
