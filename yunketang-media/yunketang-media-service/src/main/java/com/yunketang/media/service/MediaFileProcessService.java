package com.yunketang.media.service;

import com.yunketang.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {
    /**
     * 获取待处理任务
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取记录数
     * @return 待处理任务集合
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 开启一个任务
     *
     * @param id 任务 id
     * @return true 开启任务成功，false 开启任务失败
     */
    public boolean startTask(long id);

    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);
}
