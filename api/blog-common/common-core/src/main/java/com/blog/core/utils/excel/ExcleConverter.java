package com.blog.core.utils.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-16
 */

public class ExcleConverter implements Converter<Integer> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData,
                                     ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) {
        String text = cellData.getStringValue();
        if (contentProperty == null || contentProperty.getField() == null) {
            return null;
        }

        ExcelValueMap annotation = contentProperty.getField().getAnnotation(ExcelValueMap.class);
        if (annotation == null) {
            return null;
        }

        Map<String, Integer> mapping = parseMapping(annotation.valueMapping());
        return mapping.getOrDefault(text, annotation.readDefault());
    }

    @Override
    public WriteCellData<?> convertToExcelData(Integer value,
                                               ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        if (contentProperty == null || contentProperty.getField() == null) {
            return new WriteCellData<>(value != null ? value.toString() : "");
        }

        ExcelValueMap annotation = contentProperty.getField().getAnnotation(ExcelValueMap.class);
        if (annotation == null) {
            return new WriteCellData<>(value != null ? value.toString() : "");
        }

        Map<Integer, String> reverseMapping = parseReverseMapping(annotation.valueMapping());
        return new WriteCellData<>(reverseMapping.getOrDefault(value, annotation.writeDefault()));
    }

    // 解析映射关系：文本 -> 数字
    private Map<String, Integer> parseMapping(String mappingStr) {
        Map<String, Integer> map = new HashMap<>();
        if (StringUtils.isEmpty(mappingStr)) {
            return map;
        }

        String[] pairs = mappingStr.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                try {
                    int key = Integer.parseInt(kv[0].trim());
                    map.put(kv[1].trim(), key);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }

    // 解析反向映射关系：数字 -> 文本
    private Map<Integer, String> parseReverseMapping(String mappingStr) {
        Map<Integer, String> map = new HashMap<>();
        if (StringUtils.isEmpty(mappingStr)) {
            return map;
        }

        String[] pairs = mappingStr.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                try {
                    int key = Integer.parseInt(kv[0].trim());
                    map.put(key, kv[1].trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }
}
