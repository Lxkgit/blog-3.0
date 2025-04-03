package com.blog.content.service;



import com.blog.core.domain.content.diary.entity.Diary;
import com.blog.core.domain.content.diary.vo.DiaryVo;
import com.blog.core.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface DiaryService {

    /**
     * 新增日记接口
     *
     * @param diaryVo
     * @return
     */
    Integer saveDiary(DiaryVo diaryVo);

    /**
     * 删除日记接口
     *
     * @param ids
     * @return
     */
    Integer deleteDiary(String ids) throws ServiceException;

    /**
     * 修改日记接口
     *
     * @param diaryVo
     * @return
     */
    Integer updateDiary(DiaryVo diaryVo);

    /**
     * 查询日记接口
     *
     * @param diaryVo
     * @return
     */
    Map<String, Object> selectDiaryByDate(DiaryVo diaryVo);

    Map<String, List<String>> saveDiaryList(Map<String, Diary> map);


}