package com.blog.file.controller;

import com.blog.core.domain.file.calendar.entity.BlogCalendar;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.service.CalendarService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description 博客日历
 * @Author lxk
 * @CreateTime 2026-09-22
 */

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    @Resource
    private CalendarService calendarService;

    /**
     * 新增日历记录
     */
    @PostMapping("/save")
    public Result save(@RequestBody BlogCalendar calendar) {
        return ResultFactory.buildSuccessResult(calendarService.save(calendar));
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/select/id")
    public Result getById(@RequestParam("id") Integer id) {
        return ResultFactory.buildSuccessResult(calendarService.getById(id));
    }

    /**
     * 查询指定月份的全部记录
     * 示例：GET /calendar/select/month?year=2026&month=9
     */
    @GetMapping("/select/month")
    public Result listByMonth(@RequestParam("year") Integer year, @RequestParam("month") Integer month) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        return ResultFactory.buildSuccessResult(calendarService.listByMonth(userId, year, month));
    }

    /**
     * 修改日历记录
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody BlogCalendar calendar) {
        return ResultFactory.buildSuccessResult(calendarService.updateById(calendar));
    }

    /**
     * 删除日历记录
     */
    @DeleteMapping("/delete/{id}")
    public Result removeById(@PathVariable Long id) {
        return ResultFactory.buildSuccessResult(calendarService.removeById(id));
    }
}
