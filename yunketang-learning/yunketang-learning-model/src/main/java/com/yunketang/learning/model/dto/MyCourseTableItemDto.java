package com.yunketang.learning.model.dto;

import com.yunketang.learning.model.po.CourseTables;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 *  课程查询条件
 */
@Data
@ToString
public class MyCourseTableItemDto extends CourseTables {

    /**
     * 最近学习时间
     */
    private LocalDateTime learnDate;

    /**
     * 学习时长
     */
    private Long learnLength;

    /**
     * 章节id
     */
    private Long teachplanId;

    /**
     * 章节名称
     */
    private String teachplanName;


}
