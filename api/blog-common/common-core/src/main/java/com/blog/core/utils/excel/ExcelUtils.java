package com.blog.core.utils.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.WriteHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Description excel导出工具
 * @Author lxk
 * @CreateTime 2025-07-11
 */

public class ExcelUtils<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * 实体对象
     */
    public Class<T> clazz;

    public ExcelUtils(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void exportExcel(HttpServletResponse response, String fileName, List<String> headList, List<T> dateList) {
        exportExcel(response, fileName, headList, dateList, new BaseExcelHeadHandler(headList));
    }

    public void exportExcel(HttpServletResponse response, String fileName, List<String> headList, List<T> dateList, WriteHandler writeHandler) {
        try {
            Integer startLine = CollectionUtils.isEmpty(headList) ? 0 : headList.size();

            //HttpServletResponse消息头参数设置
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "public");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");

            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), StandardCharsets.ISO_8859_1));

            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(writeHandler)
                    .sheet()
                    .useDefaultStyle(true).relativeHeadRowIndex(startLine)
                    // 表格数据
                    .doWrite(dateList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
