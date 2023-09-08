package com.yunketang.search.controller;

import com.yunketang.base.exception.YunketangException;
import com.yunketang.search.po.CourseIndex;
import com.yunketang.search.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程索引接口
 */
@Api(value = "课程信息索引接口", tags = "课程信息索引接口")
@RestController
@RequestMapping("/index")
public class CourseIndexController {

    @Autowired
    private IndexService indexService;
    @Value("${elasticsearch.course.index}")
    private String courseIndexName;

    @ApiOperation("添加课程信息文档")
    @PostMapping("/course")
    public Boolean add(@RequestBody CourseIndex courseIndex) {
        Long id = courseIndex.getId();
        if (id == null) {
            YunketangException.cast("课程id为空");
        }
        Boolean result = indexService.addCourseIndex(courseIndexName, String.valueOf(id), courseIndex);
        if (!result) {
            YunketangException.cast("添加课程索引失败！");
        }
        return true;
    }
}
