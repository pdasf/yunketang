package com.yunketang.learning.api;

import com.yunketang.base.exception.YunketangException;
import com.yunketang.base.model.PageResult;
import com.yunketang.learning.model.dto.MyCourseTableParams;
import com.yunketang.learning.model.dto.ChooseCourseDto;
import com.yunketang.learning.model.dto.CourseTablesDto;
import com.yunketang.learning.model.po.CourseTables;
import com.yunketang.learning.service.MyCourseTablesService;
import com.yunketang.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {
    @Autowired
    MyCourseTablesService myCourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public ChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        SecurityUtil.User user = SecurityUtil.getUser();
        if (user == null) {
            YunketangException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        return myCourseTablesService.addChooseCourse(userId, courseId);
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public CourseTablesDto getLearnstatus(@PathVariable Long courseId) {
        SecurityUtil.User user = SecurityUtil.getUser();
        if (user == null) {
            YunketangException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        return myCourseTablesService.getLearningStatus(userId, courseId);
    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<CourseTables> myCourseTables(MyCourseTableParams params) {
        SecurityUtil.User user = SecurityUtil.getUser();
        if (user == null) {
            YunketangException.cast("请登录后查看课程表");
        }
        String userId = user.getId();
        params.setUserId(userId);
        return myCourseTablesService.myCourseTables(params);
    }
}
