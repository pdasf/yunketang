package com.yunketang.content.api;

import com.yunketang.content.model.dto.CoursePreviewDto;
import com.yunketang.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "课程公开查询接口", tags = "课程公开查询接口")
@Slf4j
@RestController
@RequestMapping("/open")
public class CourseOpenController {
    @Autowired
    private CoursePublishService coursePublishService;

    @ApiOperation("课程查询接口")
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable Long courseId) {
        // 获取课程预览信息
        return coursePublishService.getCoursePreviewInfo(courseId);
    }
}
