package com.blog.file.service;

import com.blog.core.domain.file.calendar.entity.BlogCalendar;

import java.util.List;

/**
 * @author lxk
 * @description 日历服务
 * @date 2026/09/22
 */

public interface CalendarService {

    /**
     * 新增
     */
    boolean save(BlogCalendar calendar);

    /**
     * 根据ID查询
     */
    BlogCalendar getById(Integer id);

    /**
     * 查询全部
     */
    List<BlogCalendar> list();

    /**
     * 修改
     */
    boolean updateById(BlogCalendar calendar);

    /**
     * 删除
     */
    boolean removeById(Integer id);

    /**
     * 查询指定月份的全部日历记录
     *
     * @param userId 用户ID
     * @param year    年
     * @param month   月
     */
    List<BlogCalendar> listByMonth(Integer userId, int year, int month);
}
