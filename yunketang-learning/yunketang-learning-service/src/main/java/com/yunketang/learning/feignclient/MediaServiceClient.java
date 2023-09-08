package com.yunketang.learning.feignclient;

import com.yunketang.base.model.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 远程调用媒资管理服务
 */
@FeignClient(value = "media-api", fallbackFactory = MediaServiceClientFallbackFactory.class)
@RequestMapping("/media")
public interface MediaServiceClient {

    /**
     * 获取媒资url
     *
     * @param mediaId 媒资id
     * @return
     */
    @GetMapping("/preview/{mediaId}")
    RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId);
}
