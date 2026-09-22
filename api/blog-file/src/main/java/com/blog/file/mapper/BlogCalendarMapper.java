package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.calendar.entity.BlogCalendar;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lxk
 * @description 博客日历 Mapper
 * @date 2026/09/22
 */

@Mapper
public interface BlogCalendarMapper extends BaseMapper<BlogCalendar> {

}
