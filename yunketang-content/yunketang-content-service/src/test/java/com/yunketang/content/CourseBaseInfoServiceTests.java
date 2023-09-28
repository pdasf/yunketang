package com.yunketang.content;

import com.yunketang.base.model.PageParams;
import com.yunketang.base.model.PageResult;
import com.yunketang.content.model.dto.QueryCourseParamDto;
import com.yunketang.content.model.po.CourseBase;
import com.yunketang.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseBaseInfoServiceTests {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testCourseBaseInfoService() {

        //查询条件
        QueryCourseParamDto courseParamsDto = new QueryCourseParamDto();
        courseParamsDto.setCourseName("java");//课程名称查询条件
        courseParamsDto.setAuditStatus("202004");//202004表示课程审核通过
        //分页参数对象
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(2L);
        pageParams.setPageSize(2L);

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(null,pageParams, courseParamsDto);
        System.out.println(courseBasePageResult);

    }
}
