package com.blog.file.controller;

import com.blog.core.domain.file.files.vo.ImportDiaryVo;
import com.blog.core.domain.file.files.vo.FileUploadVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.AddGroup;
import com.blog.file.service.ImportService;
import com.blog.file.service.UploadFileService;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * @Author: lxk
 * @date 2022/7/7 15:42
 * @description: 文件上传接口服务
 */

@RestController
@RequestMapping("/upload")
public class UploadFileController {

    @Resource
    private UploadFileService fileUploadService;

    @Resource
    private ImportService importService;

    @Resource
    private MinioClient minioClient;

    /**
     * 上传单个文件
     *
     * @param uploadVo
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('sys:file:user:upload')")
    public Result uploadFile(@Validated(value = {AddGroup.class}) FileUploadVo uploadVo) throws ServiceException {
        return ResultFactory.buildSuccessResult(fileUploadService.uploadService(uploadVo));
    }

//    @GetMapping("/images/**")
//    public String getImageUrl(HttpServletRequest request) {
//        return request.getRequestURI().split("/images/")[1];
////        String objectName = request.getRequestURI().split("/images/")[1];
////        try {
////            String url = minioClient.getPresignedObjectUrl(
////                    GetPresignedObjectUrlArgs.builder()
////                            .method(Method.GET)
////                            .bucket("blog")
////                            .object(objectName)
////                            .expiry(5, TimeUnit.MINUTES)
////                            .build()
////            );
////            return ResponseEntity.ok(url);
////        } catch (Exception e) {
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating URL");
////        }
//    }

    // http://localhost:60001/file/upload/images/1/user/2025-04-13_19:22:01_3ced7e_1.jpg
    @GetMapping("/images/**")
    public ResponseEntity<StreamingResponseBody> getImageUr2l(HttpServletRequest request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String objectName = URLDecoder.decode(request.getRequestURI().split("/images/")[1], StandardCharsets.UTF_8);

        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket("blog")
                        .object(objectName)
                        .build()
        );

        // 3. 动态设置Content-Type
        String contentType = determineContentType(objectName); // 实现此方法
        if (contentType == null || contentType.isEmpty()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // 4. 构建流式响应
        // 流在此处自动关闭
        StreamingResponseBody body = stream::transferTo;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(body);
    }

    // 根据文件名解析Content-Type
    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

    @PostMapping("/diary/import")
    public Result importDiary(@RequestBody ImportDiaryVo importDiaryVo) {
//        BlogUser blogUser = getBlogUser(request);
        importDiaryVo.setUserId(1);
        if (importService.importDiary(importDiaryVo)) {
            return ResultFactory.buildSuccessResult();
        } else {
            return ResultFactory.buildFailResult("部分日记上传失败");
        }
    }

//    @GetMapping("/export")
//    public void exportImg(HttpServletResponse response) throws IOException {
//        List<String> strings = new ArrayList<>();
//        String str1 = "http://172.25.238.129:9876/65fbf56f-1930-11ed-b10f-d094663eb298/20220929/1/b66ee81d-3fa1-11ed-9910-d094663eb298.png";
//        String str2 = "http://172.25.238.129:9876/65fbf56f-1930-11ed-b10f-d094663eb298/20220929/1/b66ee81d-3fa1-11ed-9910-d094663eb298.png";
//        strings.add(str1);
//        strings.add(str2);
//        ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
//        //设置返回响应头
//        response.reset();
//        // 自动判断下载文件类型
//        response.setContentType("multipart/form-data");
//        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("picture.zip", "UTF-8"));
//        try {
//            for (int i=0; i<strings.size(); i++) {
//                URL url = new URL(strings.get(i));
//                //打开链接
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                //设置请求方式为"GET"
//                conn.setRequestMethod("GET");
//                //超时响应时间为5秒
//                conn.setConnectTimeout(5 * 1000);
//                //通过输入流获取图片数据
//                InputStream inStream = conn.getInputStream();
//                //得到图片的二进制数据，以二进制封装得到数据，具有通用性
//                byte[] data = readInputStream(inStream);
//                //重点开始，创建压缩文件
//                ZipEntry zipEntry = new ZipEntry( i + ".png");
//                zipOutputStream.putNextEntry(zipEntry);
//                zipOutputStream.write(data);
//                inStream.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                zipOutputStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 得到图片的二进制数据，以二进制封装得到数据，具有通用性
//     *
//     * @param inStream
//     * @return
//     * @throws Exception
//     */
//    private byte[] readInputStream(InputStream inStream) throws Exception {
//        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//        //创建一个Buffer字符串
//        byte[] buffer = new byte[1024];
//        //每次读取的字符串长度，如果为-1，代表全部读取完毕
//        int len = 0;
//        //使用一个输入流从buffer里把数据读取出来
//        while ((len = inStream.read(buffer)) != -1) {
//            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
//            outStream.write(buffer, 0, len);
//        }
//        //关闭输入流
//        inStream.close();
//        //把outStream里的数据写入内存
//        return outStream.toByteArray();
//    }
//
//    //使用easyExcel导出
//    @RequestMapping(value = "/download", method = RequestMethod.GET)
//    public void easyExcelExport(HttpServletResponse response) {
//        String fileName;
//        try {
////            //获取需要导出的数据
//            List<UserInfo> dataList = getData();
//            fileName = new String("test".getBytes(), StandardCharsets.UTF_8);
//            response.setContentType("application/vnd.ms-excel");
//            response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
//            // 表一写入
//            ExcelWriter writer = EasyExcel.write(response.getOutputStream(), UserInfo.class).build();
//            WriteSheet sheet = EasyExcel.writerSheet(0, "基础信息").build();
//            writer.write(dataList, sheet);
//
//            // 表二写入
//            WriteSheet sheet2 = EasyExcel.writerSheet(1, "详细信息").head(UserInfo.class).build();
//            writer.write(dataList, sheet2);
//            // 关闭流
//            writer.finish();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //设置数据
//    private List<UserInfo> getData() {
//        List<UserInfo> list = new ArrayList<>();
//        list.add(new UserInfo("张三", "男", 18, "189cm", "唱歌"));
//        list.add(new UserInfo("李四", "女", 16, "160cm", "跳舞"));
//        return list;
//    }
//
//    @GetMapping("/getImg/{filename}")
//    public void getImg(HttpServletResponse response, @PathVariable("filename") String filename){
//        log.info("/getImg->访问图片->开始" );
//
//        String rootPath = "D:/img";
//        String filePath = rootPath + "/" + filename;
//        File imageFile = new File(filePath);
//        if (imageFile.exists()){
//            FileInputStream fis = null;
//            OutputStream os = null;
//            try {
//                fis = new FileInputStream(imageFile);
//                os = response.getOutputStream();
//                int count = 0;
//                byte[] buffer = new byte[1024 * 8];
//                while((count = fis.read(buffer)) != -1){
//                    os.write(buffer,0,count);
//                    os.flush();
//                }
//                log.info("[图片接口]输出完成");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }finally {
//                if(fis != null){
//                    try {
//                        fis.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if(os != null){
//                    try {
//                        os.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//
//                    }
//                }
//
//            }
//
//        }else{
//
//        }
//    }

}
