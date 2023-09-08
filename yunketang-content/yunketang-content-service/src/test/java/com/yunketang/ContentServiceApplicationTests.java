package com.yunketang;

import com.yunketang.base.model.PageParams;
import com.yunketang.base.model.PageResult;
import com.yunketang.content.mapper.CourseBaseMapper;
import com.yunketang.content.model.dto.CourseCategoryTreeDto;
import com.yunketang.content.model.dto.QueryCourseParamDto;
import com.yunketang.content.model.po.CourseBase;
import com.yunketang.content.service.CourseBaseInfoService;
import com.yunketang.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@SpringBootTest(classes = ContentServiceApplication.class)
class YunketangContentServiceApplicationTests {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Resource
    CourseCategoryService courseCategoryService;

    @Test
    void contextLoads() {
        CourseBase courseBase = courseBaseMapper.selectById(22);
        log.info("查询到数据：{}", courseBase);
        Assertions.assertNotNull(courseBase);
    }

    @Test
    void contextQueryCourseTest() {
        PageResult<CourseBase> result = courseBaseInfoService.queryCourseBaseList(null, new PageParams(1L, 10L), new QueryCourseParamDto());
        log.info("查询到数据：{}", result);
    }

    @Test
    void contextCourseCategoryTest() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
