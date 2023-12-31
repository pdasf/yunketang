package com.yunketang.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunketang.base.exception.YunketangException;
import com.yunketang.base.model.PageResult;
import com.yunketang.content.model.po.CoursePublish;
import com.yunketang.learning.feignclient.ContentServiceClient;
import com.yunketang.learning.mapper.ChooseCourseMapper;
import com.yunketang.learning.mapper.CourseTablesMapper;
import com.yunketang.learning.model.dto.ChooseCourseDto;
import com.yunketang.learning.model.dto.CourseTablesDto;
import com.yunketang.learning.model.dto.MyCourseTableParams;
import com.yunketang.learning.model.po.ChooseCourse;
import com.yunketang.learning.model.po.CourseTables;
import com.yunketang.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class MyCourseTablesServiceImpl implements MyCourseTablesService {
    @Autowired
    MyCourseTablesService myCourseTablesService;
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    ChooseCourseMapper chooseCourseMapper;
    @Autowired
    CourseTablesMapper courseTablesMapper;

    @Override
    @Transactional
    public ChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // 1. 选课调用内容管理服务提供的查询课程接口，查询课程收费规则
        // 1.1 查询课程
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(courseId);
        if (coursePublish == null) {
            YunketangException.cast("课程不存在");
        }
        // 1.2 获取收费规则
        String charge = coursePublish.getCharge();
        ChooseCourse chooseCourse = null;
        if ("201000".equals(charge)) {
            // 2. 如果是免费课程，向选课记录表、我的课程表添加数据
            log.info("添加免费课程..");
            chooseCourse = myCourseTablesService.addFreeCourse(userId, coursePublish);
            addCourseTables(chooseCourse);
        } else {
            // 3. 如果是收费课程，向选课记录表添加数据
            log.info("添加收费课程");
            chooseCourse = myCourseTablesService.addChargeCourse(userId, coursePublish);
        }

        // 4. 获取学生的学习资格
        CourseTablesDto courseTablesDto = getLearningStatus(userId, courseId);
        // 5. 封装返回值
        ChooseCourseDto chooseCourseDto = new ChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse, chooseCourseDto);
        chooseCourseDto.setLearnStatus(courseTablesDto.learnStatus);
        return chooseCourseDto;
    }

    /**
     * 判断学习资格
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 学习资格状态：查询数据字典 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     */
    @Override
    public CourseTablesDto getLearningStatus(String userId, Long courseId) {
        CourseTablesDto courseTablesDto = new CourseTablesDto();
        // 1. 查询我的课程表
        CourseTables courseTables = getCourseTables(userId, courseId);
        // 2. 未查到，返回一个状态码为"702002"的对象
        if (courseTables == null) {
            courseTablesDto = new CourseTablesDto();
            courseTablesDto.setLearnStatus("702002");
            return courseTablesDto;
        }
        // 3. 查到了，判断是否过期
        boolean isExpires = LocalDateTime.now().isAfter(courseTables.getValidtimeEnd());
        // 3.1 已过期，返回状态码为"702003"的对象
        if (isExpires) {
            BeanUtils.copyProperties(courseTables, courseTablesDto);
            courseTablesDto.setLearnStatus("702003");
            return courseTablesDto;
        }
        // 3.2 未过期，返回状态码为"702001"的对象
        else {
            BeanUtils.copyProperties(courseTables, courseTablesDto);
            courseTablesDto.setLearnStatus("702001");
            return courseTablesDto;
        }
    }

    /**
     * 添加到我的课程表
     *
     * @param chooseCourse 选课记录
     */
    @Transactional
    public CourseTables addCourseTables(ChooseCourse chooseCourse) {
        String status = chooseCourse.getStatus();
        if (!"701001".equals(status)) {
            YunketangException.cast("选课未成功，无法添加到课程表");
        }
        CourseTables courseTables = getCourseTables(chooseCourse.getUserId(), chooseCourse.getCourseId());
        if (courseTables != null) {
            return courseTables;
        }
        courseTables = new CourseTables();
        BeanUtils.copyProperties(chooseCourse, courseTables);
        courseTables.setChooseCourseId(chooseCourse.getId());
        courseTables.setCourseType(chooseCourse.getOrderType());
        courseTables.setUpdateDate(LocalDateTime.now());
        int insert = courseTablesMapper.insert(courseTables);
        if (insert <= 0) {
            YunketangException.cast("添加我的课程表失败");
        }
        return courseTables;
    }

    /**
     * 根据用户id和课程id查询我的课程表中的某一门课程
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 我的课程表中的课程
     */
    public CourseTables getCourseTables(String userId, Long courseId) {
        return courseTablesMapper.selectOne(new LambdaQueryWrapper<CourseTables>()
                .eq(CourseTables::getUserId, userId)
                .eq(CourseTables::getCourseId, courseId));
    }

    /**
     * 将付费课程加入到选课记录表
     *
     * @param userId        用户id
     * @param coursePublish 课程发布信息
     * @return 选课记录
     */
    @Transactional
    public ChooseCourse addChargeCourse(String userId, CoursePublish coursePublish) {
        // 1. 先判断是否已经存在对应的选课，因为数据库中没有约束，所以可能存在相同数据的选课
        LambdaQueryWrapper<ChooseCourse> lambdaQueryWrapper = new LambdaQueryWrapper<ChooseCourse>()
                .eq(ChooseCourse::getUserId, userId)
                .eq(ChooseCourse::getCourseId, coursePublish.getId())
                .eq(ChooseCourse::getOrderType, "700002")  // 收费课程
                .eq(ChooseCourse::getStatus, "701002");// 待支付
        // 1.1 由于可能存在多条，所以这里用selectList
        List<ChooseCourse> chooseCourses = chooseCourseMapper.selectList(lambdaQueryWrapper);
        // 1.2 如果已经存在对应的选课数据，返回一条即可
        if (!chooseCourses.isEmpty()) {
            return chooseCourses.get(0);
        }
        // 2. 数据库中不存在数据，添加选课信息，对照着数据库中的属性挨个set即可
        ChooseCourse chooseCourse = new ChooseCourse();
        chooseCourse.setCourseId(coursePublish.getId());
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setOrderType("700002");
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursePublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701002");
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = chooseCourseMapper.insert(chooseCourse);
        if (insert <= 0) {
            YunketangException.cast("添加选课记录失败");
        }
        return chooseCourse;
    }

    @Override
    @Transactional
    public boolean saveChooseCourseStatus(String chooseCourseId) {
        // 1. 根据选课id，查询选课表
        ChooseCourse chooseCourse = chooseCourseMapper.selectById(chooseCourseId);
        if (chooseCourse == null) {
            log.error("接收到购买课程的消息，根据选课id未查询到课程，选课id：{}", chooseCourseId);
            return false;
        }
        // 2. 选课状态为未支付时，更新选课状态为选课成功
        if ("701002".equals(chooseCourse.getStatus())) {
            chooseCourse.setStatus("701001");
            int update = chooseCourseMapper.updateById(chooseCourse);
            if (update <= 0) {
                log.error("更新选课记录失败：{}", chooseCourse);
            }
        }
        // 3. 向我的课程表添加记录
        addCourseTables(chooseCourse);
        return true;
    }

    @Override
    public PageResult<CourseTables> myCourseTables(MyCourseTableParams params) {
        // 1. 获取页码
        int pageNo = params.getPage();
        // 2. 设置每页记录数，固定为4
        long pageSize = 4;
        // 3. 分页条件
        Page<CourseTables> page = new Page<>(pageNo, pageSize);
        // 4. 根据用户id查询课程
        String userId = params.getUserId();
        LambdaQueryWrapper<CourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTables::getUserId, userId);
        // 5. 分页查询
        Page<CourseTables> pageResult = courseTablesMapper.selectPage(page, queryWrapper);
        // 6. 获取记录总数
        long total = pageResult.getTotal();
        // 7. 获取记录
        List<CourseTables> records = pageResult.getRecords();
        // 8. 封装返回
        return new PageResult<>(records, total, pageNo, pageSize);
    }

    /**
     * 将免费课程加入到选课表
     *
     * @param userId        用户id
     * @param coursePublish 课程发布信息
     * @return 选课记录
     */
    @Transactional
    public ChooseCourse addFreeCourse(String userId, CoursePublish coursePublish) {
        // 1. 先判断是否已经存在对应的选课，因为数据库中没有约束，所以可能存在相同数据的选课
        LambdaQueryWrapper<ChooseCourse> lambdaQueryWrapper = new LambdaQueryWrapper<ChooseCourse>()
                .eq(ChooseCourse::getUserId, userId)
                .eq(ChooseCourse::getCourseId, coursePublish.getId())
                .eq(ChooseCourse::getOrderType, "700001")  // 免费课程
                .eq(ChooseCourse::getStatus, "701001");// 选课成功
        // 1.1 由于可能存在多条，所以这里用selectList
        List<ChooseCourse> chooseCourses = chooseCourseMapper.selectList(lambdaQueryWrapper);
        // 1.2 如果已经存在对应的选课数据，返回一条即可
        if (!chooseCourses.isEmpty()) {
            return chooseCourses.get(0);
        }
        // 2. 数据库中不存在数据，添加选课信息，对照着数据库中的属性挨个set即可
        ChooseCourse chooseCourse = new ChooseCourse();
        chooseCourse.setCourseId(coursePublish.getId());
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setOrderType("700001");
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursePublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701001");
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = chooseCourseMapper.insert(chooseCourse);
        if (insert <= 0) {
            YunketangException.cast("添加选课记录失败");
        }
        return chooseCourse;
    }
}
