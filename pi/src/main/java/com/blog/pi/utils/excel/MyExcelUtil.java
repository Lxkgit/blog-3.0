package com.blog.pi.utils.excel;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.blog.pi.utils.excel.demo.PlayerRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-10
 */

public class MyExcelUtil {

    /**
     * 通过MultipartFile读取Excel文件，适用web端上传的Excel文件
     *
     * @param file        文件
     * @param sheetRowNum 表头行数
     * @return List 封装的实体类列表
     */
    public static List<PlayerRecord> readExcel(MultipartFile file, int sheetRowNum) throws IOException {
        List<PlayerRecord> list = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), PlayerRecord.class, new AnalysisEventListener<PlayerRecord>() {
            @Override
            public void invoke(PlayerRecord data, AnalysisContext context) {
//                if (data.getLocalDate() != null) {
//                    // 这里可以添加任何额外操作逻辑
//                    data.setDay(data.getLocalDate().getDayOfMonth());
//                    data.setMonth(data.getLocalDate().getMonthValue());
//                    data.setYear(data.getLocalDate().getYear());
//                    data.setQuarter(data.getLocalDate().getMonthValue() / 3 + 1);
//                }
                list.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }).sheet().headRowNumber(sheetRowNum).doRead();
        return list;
    }

    /**
     * File读取Excel文件，适用本地文件读取的Excel文件
     *
     * @param file        文件
     * @param sheetRowNum 表头行数
     * @return List 封装的实体类列表
     */
    public static List<PlayerRecord> readExcel(File file, int sheetRowNum) throws IOException {
        List<PlayerRecord> list = new ArrayList<>();
        EasyExcel.read(file, PlayerRecord.class, new AnalysisEventListener<PlayerRecord>() {
            @Override
            public void invoke(PlayerRecord data, AnalysisContext context) {
//                if (data.getLocalDate() != null) {
//                    // 这里可以添加任何额外操作逻辑
//                    data.setDay(data.getLocalDate().getDayOfMonth());
//                    data.setMonth(data.getLocalDate().getMonthValue());
//                    data.setYear(data.getLocalDate().getYear());
//                    data.setQuarter(data.getLocalDate().getMonthValue() / 3 + 1);
//                }
                list.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }).sheet().headRowNumber(sheetRowNum).doRead();
        return list;
    }
}