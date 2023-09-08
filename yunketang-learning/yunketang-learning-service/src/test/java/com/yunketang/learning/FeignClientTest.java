package com.yunketang.learning;

import com.yunketang.content.model.po.CoursePublish;
import com.yunketang.learning.feignclient.ContentServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * @description TODO
 */
@SpringBootTest
public class FeignClientTest {

    @Autowired
    ContentServiceClient contentServiceClient;

    @Test
    public void testContentServiceClient() {
        CoursePublish coursepublish = contentServiceClient.getCoursePublish(160L);
        System.out.println(coursepublish);
    }
}