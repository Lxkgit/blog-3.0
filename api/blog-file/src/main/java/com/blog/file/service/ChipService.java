package com.blog.file.service;

import com.blog.core.domain.file.device.vo.ChipVo;
import com.blog.core.exception.ValidException;
import com.blog.core.result.MyPage;

/**
 * @description: 单片机服务层
 * @Author: lxk
 * @date 2024/1/30 19:57
 */

public interface ChipService {

    Integer addChip(Integer userId, ChipVo chipVo) throws ValidException;

    Integer deleteChips(Integer userId, String ids);

    Integer updateChip(Integer userId, ChipVo chipVo) throws ValidException;

    MyPage<ChipVo> selectChipList(Integer userId, ChipVo chipVo);

    ChipVo selectChipId(Integer userId, Integer id) throws ValidException;

    ChipVo selectChipInfo(Integer userId, ChipVo chipVo);

}
