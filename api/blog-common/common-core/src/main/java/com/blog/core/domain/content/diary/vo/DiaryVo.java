package com.blog.core.domain.content.diary.vo;

import com.blog.core.domain.content.diary.entity.Diary;
import com.blog.core.valication.annotation.Equal;
import com.blog.core.valication.group.AddGroup;
import com.blog.core.valication.group.DeleteGroup;
import com.blog.core.valication.group.UpdateGroup;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: lxk
 * @date 2022/12/28 17:26
 * @description:
 */

@Getter
@Setter
public class DiaryVo extends Diary {

    private Date diaryDate;

    @Equal(value = "0,1", message = "日记状态（1：发布 0：草稿）字段错误", groups = {AddGroup.class, UpdateGroup.class})
    private Integer diaryStatus;

    @Pattern(regexp = "^[0-9]+(,[0-9]+)+|[0-9]+$", message = "请输入正确的日记id字符串", groups = {DeleteGroup.class})
    private String ids;

    private Integer pageSize;

    private Integer pageNum;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String date;

    @DateTimeFormat(pattern = "yyyy-MM")
    private String dateMonth;
}
