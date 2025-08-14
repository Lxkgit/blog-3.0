package com.blog.file.service;

import com.blog.core.domain.file.device.vo.ChipVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.ResultPage;

/**
 * @description: 单片机服务层
 * @Author: lxk
 * @date 2024/1/30 19:57
 */

public interface ChipService {

    Integer addChip(ChipVo chipVo) throws ServiceException;

    Integer deleteChips(String ids);

    Integer updateChip(ChipVo chipVo) throws ServiceException;

    ResultPage<ChipVo> selectChipList(ChipVo chipVo);

    ChipVo selectChipId(Integer id) throws ServiceException;

    ChipVo selectChipInfo(ChipVo chipVo);

}
