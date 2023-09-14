package com.yunketang.media.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yunketang.base.exception.YunketangException;
import com.yunketang.base.utils.Mp4VideoUtil;
import com.yunketang.media.mapper.MediaProcessMapper;
import com.yunketang.media.model.po.MediaProcess;
import com.yunketang.media.service.MediaFileProcessService;
import com.yunketang.media.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VideoTask {
    @Value("${videoprocess.ffmpegpath}")
    String ffmpegPath;
    @Autowired
    private MediaProcessMapper mediaProcessMapper;
    @Autowired
    private MediaFileService mediaFileService;
    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws InterruptedException {
        // 分片序号
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("========== shardIndex:{}, shardTotal:{} =========", shardIndex, shardTotal);
        // 查询待处理任务，一次处理的任务数与cpu核心数相同
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardTotal, shardIndex,
                Runtime.getRuntime().availableProcessors());
        // 要处理的任务数
        int size = mediaProcessList.size();
        // 未查询到待处理任务，结束方法
        if (size == 0) {
            log.debug("查询到的待处理任务数为0");
            return;
        }

        // 查询到任务，创建size个线程去处理
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        // 计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        // 将处理任务加入线程池
        mediaProcessList.forEach(mediaProcess -> threadPool.execute(() -> {
            //任务 id
            Long taskId = mediaProcess.getId();
            //抢占任务
            boolean b = mediaFileProcessService.startTask(taskId);
            if (!b) {
                return;
            }
            log.debug("开始执行任务:{}", mediaProcess);

            // 桶
            String bucket = mediaProcess.getBucket();
            // 文件路径
            String filePath = mediaProcess.getFilePath();
            // 原始文件的md5
            String fileId = mediaProcess.getFileId();
            File originalFile = null;
            File mp4File = null;
            try {
                // 将原始视频下载到本地，创建临时文件
                originalFile = File.createTempFile("original", null);
                // 处理完成后的文件
                mp4File = File.createTempFile("mp4", ".mp4");
            } catch (IOException e) {
                log.error("处理视频前创建临时文件失败");
                mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3",
                        fileId, null, "下载待处理文件失败");
                countDownLatch.countDown();
                YunketangException.cast("处理视频前创建临时文件失败");
            }
            try {
                mediaFileService.downloadFileFromMinio(originalFile, bucket, filePath);
            } catch (Exception e) {
                log.error("下载原始文件过程中出错：{}，文件信息：{}", e.getMessage(), mediaProcess);
                mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3",
                        fileId, null, "下载待处理文件失败");
                countDownLatch.countDown();
                YunketangException.cast("下载原始文件过程出错");
            }
            // 调用工具类将avi转为mp4
            String result = null;
            try {
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, originalFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                result = videoUtil.generateMp4();
            } catch (Exception e) {
                log.error("处理视频失败，视频地址：{}，错误信息：{}", originalFile.getAbsolutePath(), e.getMessage());
                mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3",
                        fileId, null, "下载待处理文件失败");
                countDownLatch.countDown();
                YunketangException.cast("处理视频失败");
            }
            // 转换成功，上传到MinIO
            String status = "3";
            String url = null;
            if ("success".equals(result)) {
                // 根据文件md5，生成objectName
                String objectName = mediaFileService.getFilePathByMd5(fileId, ".mp4");
                try {
                    mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), bucket, objectName);
                } catch (Exception e) {
                    log.error("上传文件失败：{}", e.getMessage());
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3",
                            fileId, null, "下载待处理文件失败");
                    YunketangException.cast("上传文件失败");
                }
                status = "2";    // 处理成功
                url = "/" + bucket + "/" + objectName;
            }
            // 记录任务处理结果url
            mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), status, fileId, url, result);
            countDownLatch.countDown();
        }));
        countDownLatch.await(30, TimeUnit.MINUTES);
    }
}
