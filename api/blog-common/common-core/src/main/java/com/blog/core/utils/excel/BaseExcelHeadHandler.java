package com.blog.core.utils.excel;


import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;


public class BaseExcelHeadHandler implements SheetWriteHandler {

    private List<String> headList;

    public BaseExcelHeadHandler() {
    }

    public BaseExcelHeadHandler(List<String> headList) {
        this.headList = headList;
    }

    @Override
    public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {

    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        Sheet sheet = workbook.getSheetAt(0);

        if (CollectionUtil.isNotEmpty(headList)) {
            for (int i = 0; i < headList.size(); i++) {
                // 检查行是否存在，不存在则创建
                Row row = sheet.getRow(i);
                if (row == null) {
                    // 创建新行
                    row = sheet.createRow(i);
                }

                // 创建单元格并设置值
                Cell rowICell0 = row.createCell(0);
                rowICell0.setCellValue(headList.get(i));

                // 合并单元格（起始行，结束行，起始列，结束列）
                sheet.addMergedRegionUnsafe(new CellRangeAddress(i, i, 0, 4));
            }
        }
    }

}