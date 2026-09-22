package com.blog.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.calendar.entity.BlogCalendar;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.BlogCalendarMapper;
import com.blog.file.service.CalendarService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-09-22
 */

@Service
public class CalendarServiceImpl implements CalendarService {

    @Resource
    private BlogCalendarMapper blogCalendarMapper;

    /**
     * 新增
     */
    @Override
    public boolean save(BlogCalendar calendar) {
        calendar.setUserId(SecurityUtil.getLoginUser().getId());
        return blogCalendarMapper.insert(calendar) > 0;
    }

    /**
     * 根据ID查询
     */
    @Override
    public BlogCalendar getById(Integer id) {
        return blogCalendarMapper.selectById(id);
    }

    /**
     * 查询全部
     */
    @Override
    public List<BlogCalendar> list() {
        return blogCalendarMapper.selectList(null);
    }

    /**
     * 修改
     */
    @Override
    public boolean updateById(BlogCalendar calendar) {
        return blogCalendarMapper.updateById(calendar) > 0;
    }

    /**
     * 删除
     */
    @Override
    public boolean removeById(Long id) {
        return blogCalendarMapper.deleteById(id) > 0;
    }

    /**
     * 查询指定月份的全部日历记录
     */
    @Override
    public List<BlogCalendar> listByMonth(Integer userId, int year, int month) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date startDate = sdf.parse(String.format("%04d-%02d-01", year, month));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.MONTH, 1);

            Date endDate = calendar.getTime();

            LambdaQueryWrapper<BlogCalendar> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogCalendar::getUserId, userId)
                    .ge(BlogCalendar::getEventDate, startDate)
                    .lt(BlogCalendar::getEventDate, endDate)
                    .orderByAsc(BlogCalendar::getEventDate)
                    .orderByAsc(BlogCalendar::getStartTime)
                    .orderByAsc(BlogCalendar::getSort);

            return blogCalendarMapper.selectList(wrapper);

        } catch (ParseException e) {
            throw new RuntimeException("日期解析失败", e);
        }
    }
}
