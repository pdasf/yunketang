package com.yunketang.learning.service;

import com.yunketang.base.model.PageResult;
import com.yunketang.content.model.po.CoursePublish;
import com.yunketang.learning.model.dto.MyCourseTableParams;
import com.yunketang.learning.model.dto.ChooseCourseDto;
import com.yunketang.learning.model.dto.CourseTablesDto;
import com.yunketang.learning.model.po.ChooseCourse;
import com.yunketang.learning.model.po.CourseTables;

public interface MyCourseTablesService {
    /**
     * 添加选课
     *
     * @param userId   用户id
     * @param courseId 课程id
     */
    ChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 获取学习资格
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 学习资格状态
     */
    CourseTablesDto getLearningStatus(String userId, Long courseId);

    ChooseCourse addFreeCourse(String userId, CoursePublish coursePublish);

    ChooseCourse addChargeCourse(String userId, CoursePublish coursePublish);

    boolean saveChooseCourseStatus(String chooseCourseId);

    PageResult<CourseTables> myCourseTables(MyCourseTableParams params);
}
