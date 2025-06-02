package com.blog.file.xxlJob;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.exception.ServiceException;
import com.blog.file.minio.MinioService;
import com.blog.file.netty.domain.common.NettyConstant;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettyUploadBlogFileDto;
import com.blog.file.netty.domain.enums.NettyTopicEnum;
import com.blog.file.netty.service.NettyServer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileUploadSchedule {

    @Resource
    private NettyServer nettyServer;

    @Resource
    private MinioService minioService;

    public void uploadFile() {
        NettyUploadBlogFileDto dto = new NettyUploadBlogFileDto();
        dto.setServiceFilePath("");
        dto.setDeviceFilePath("");
        dto.setCount(10);
        NettyPacket<NettyUploadBlogFileDto> uploadFileRequest = NettyPacket.buildRequest(dto);
        uploadFileRequest.setTopic(NettyTopicEnum.BLOG_FILE_UPLOAD.getTopic());
        nettyServer.channelWriteByRegisterId(NettyConstant.NETTY_CLIENT1, JSONObject.toJSONString(uploadFileRequest), true);
    }

    public void fileImportMinio(NettyUploadBlogFileDto nettyUploadBlogFileDto) throws ServiceException {
        List<String> fileNameList = nettyUploadBlogFileDto.getFileNameList();
        for (String fileName : fileNameList) {
            minioService.importFile(nettyUploadBlogFileDto.getFilePath() + "/" + fileName, "/1/test");
        }

    }
}
