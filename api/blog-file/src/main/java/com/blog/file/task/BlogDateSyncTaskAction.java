package com.blog.file.task;

import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Description 定时备份博客数据
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class BlogDateSyncTaskAction implements TimerAction {

    private static final Logger logger = LoggerFactory.getLogger(BlogDateSyncTaskAction.class);

    @Resource
    private NettySyncFileService nettySyncFileService;

    /**
     * 文件同步任务-定时备份博客数据
     * 服务器端：
     * 1. 通知socket执行shell脚本，备份服务器上数据
     * 2. 等待socket备份完成数据，获取备份文件数据地址
     * 3. 发送netty文件同步消息请求
     * 设备端：
     * 1. 收到netty文件同步请求，回复消息响应
     * 2. 启动ftp下载指定目录下备份文件数据
     * 3. ftp下载完成，调用socket将备份文件移动到指定位置
     * 4. socket移动文件完成，netty再次回应消息，响应同步数据成功
     */
    @Override
    public void execute(TimerTaskContext context) {
        logger.info("开始备份博客数据");
        String filePath = nettySyncFileService.syncBlogDataFirstStep(new MsgHead());
    }

    @Override
    public String getCode() {
        return Constant.TASK_SYNC_BLOG_FILE;
    }

    @Override
    public String getName() {
        return "定时备份博客数据";
    }
}
