package com.blog.pi.utils.excel.demo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-10
 */


@Data
@ExcelIgnoreUnannotated // 忽略没有注解的字段
public class PlayerRecord {

    @ExcelProperty("姓名")
//    @ExcelProperty(index = 0)
    private String name;

    @ExcelProperty("年龄")
//    @ExcelProperty(index = 1)
    private Integer age;

    @ExcelProperty({"单机游戏", "卡普空", "游戏数量"})
//    @ExcelProperty(index = 2)
    private BigDecimal singleCapcomNum;

    @ExcelProperty({"单机游戏", "卡普空", "游戏价值"})
//    @ExcelProperty(index = 3)
    private BigDecimal singleCapcomValue;

    @ExcelProperty({"单机游戏", "2K GAMES", "游戏数量"})
//    @ExcelProperty(index = 4)
    private BigDecimal singleTaketwoNum;

    @ExcelProperty({"单机游戏", "2K GAMES", "游戏价值"})
//    @ExcelProperty(index = 5)
    private BigDecimal singleTaketwoValue;

    @ExcelProperty({"单机游戏", "战马工作室", "游戏数量"})
//    @ExcelProperty(index = 6)
    private BigDecimal singleWarhorseNum;

    @ExcelProperty({"单机游戏", "战马工作室", "游戏价值"})
//    @ExcelProperty(index = 7)
    private BigDecimal singleWarhorseValue;

    @ExcelProperty({"网络游戏", "腾讯", "游戏数量"})
//    @ExcelProperty(index = 8)
    private BigDecimal webTencentNum;

    @ExcelProperty({"网络游戏", "腾讯", "氪金总额"})
//    @ExcelProperty(index = 9)
    private BigDecimal webTencentValue;

    @ExcelProperty({"网络游戏", "网易", "游戏数量"})
//    @ExcelProperty(index = 10)
    private BigDecimal webNetEaseNum;

    @ExcelProperty({"网络游戏", "网易", "氪金总额"})
//    @ExcelProperty(index = 11)
    private BigDecimal webNetEaseValue;

    @ExcelProperty({"网络游戏", "米哈游", "游戏数量"})
//    @ExcelProperty(index = 12)
    private BigDecimal webMihoyoNum;

    @ExcelProperty({"网络游戏", "米哈游", "氪金总额"})
//    @ExcelProperty(index = 13)
    private BigDecimal webMihoyoValue;

    @ExcelProperty("职业")
//    @ExcelProperty(index = 14)
    private String job;

    @ExcelProperty("创建时间")
//    @ExcelProperty(index = 15)
    private Date localDate;

    // 测试用
    private Integer year;

    private Integer month;

    private Integer day;

    private Integer quarter;


}
