package com.yunketang.learning.feignclient;

import com.yunketang.content.model.po.CoursePublish;
import com.yunketang.content.model.po.Teachplan;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {

            @Override
            public CoursePublish getCoursePublish(Long courseId) {
                ReentrantLock lock = new ReentrantLock();
                log.error("调用内容管理服务查询课程信息发生熔断:{}", throwable.toString(), throwable);
                return null;
            }

            @Override
            public Teachplan getTeachplan(Long teachplanId) {
                log.error("调用内容管理服务查询教学计划发生熔断:{}", throwable.toString(), throwable);
                return null;
            }
        };
    }
}
