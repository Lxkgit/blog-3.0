package com.blog.file.service;

import com.blog.core.domain.file.dict.DictData;
import com.blog.core.domain.file.dict.DictType;
import com.blog.core.domain.file.dict.vo.DictDataVo;
import com.blog.core.domain.file.dict.vo.DictTypeVo;

import java.util.List;

/**
 * @author lxk
 * @description 字典服务
 * @date 2025/12/25
 */

public interface DictService {


    List<DictType> selectDictTypeList(DictTypeVo dictTypeVo);

    void insertDictType(DictType dictType);

    void deleteDictType(DictTypeVo dictTypeVo);

    void updateDictType(DictType dictType);

    DictType selectDictTypeById(DictTypeVo dictTypeVo);

    List<DictData> selectDictDataList(DictDataVo dictDataVo);

    void insertDictData(DictData dictData);

    void deleteDictData(DictDataVo dictDataVo);

    void updateDictData(DictData dictData);

    DictData selectDictDataById(DictDataVo dictDataVo);
}
