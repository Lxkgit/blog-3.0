package com.blog.file.service.impl;

import com.blog.core.domain.file.dict.DictData;
import com.blog.core.domain.file.dict.DictType;
import com.blog.core.domain.file.dict.vo.DictDataVo;
import com.blog.core.domain.file.dict.vo.DictTypeVo;
import com.blog.file.mapper.DictDataMapper;
import com.blog.file.mapper.DictTypeMapper;
import com.blog.file.service.DictService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-12-25
 */

@Service
public class DictServiceImpl implements DictService {

    @Resource
    private DictTypeMapper dictTypeMapper;

    @Resource
    private DictDataMapper dictDataMapper;

    @Override
    public List<DictType> selectDictTypeList(DictTypeVo dictTypeVo) {
        return List.of();
    }

    @Override
    public void insertDictType(DictType dictType) {

    }

    @Override
    public void deleteDictType(DictTypeVo dictTypeVo) {

    }

    @Override
    public void updateDictType(DictType dictType) {

    }

    @Override
    public DictType selectDictTypeById(DictTypeVo dictTypeVo) {
        return null;
    }

    @Override
    public List<DictData> selectDictDataList(DictDataVo dictDataVo) {
        return List.of();
    }

    @Override
    public void insertDictData(DictData dictData) {

    }

    @Override
    public void deleteDictData(DictDataVo dictDataVo) {

    }

    @Override
    public void updateDictData(DictData dictData) {

    }

    @Override
    public DictData selectDictDataById(DictDataVo dictDataVo) {
        return null;
    }
}
