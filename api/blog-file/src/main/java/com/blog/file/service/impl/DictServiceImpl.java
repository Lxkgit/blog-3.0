package com.blog.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.core.domain.file.dict.DictData;
import com.blog.core.domain.file.dict.DictType;
import com.blog.core.domain.file.dict.vo.DictDataVo;
import com.blog.core.domain.file.dict.vo.DictTypeVo;
import com.blog.file.mapper.DictDataMapper;
import com.blog.file.mapper.DictTypeMapper;
import com.blog.file.service.DictService;
import com.github.pagehelper.PageHelper;
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
        PageHelper.startPage(dictTypeVo.getPageNum(), dictTypeVo.getPageSize());
        return dictTypeMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public void insertDictType(DictType dictType) {
        dictTypeMapper.insert(dictType);
    }

    @Override
    public void deleteDictType(DictTypeVo dictTypeVo) {
        dictTypeMapper.deleteById(dictTypeVo.getId());
        dictDataMapper.deleteDictDataByDictTypeCode(dictTypeVo.getDictTypeCode());
    }

    @Override
    public void updateDictType(DictType dictType) {
        dictTypeMapper.updateById(dictType);
    }

    @Override
    public DictType selectDictTypeById(DictTypeVo dictTypeVo) {
        return dictTypeMapper.selectById(dictTypeVo.getId());
    }

    @Override
    public List<DictData> selectDictDataList(DictDataVo dictDataVo) {
        PageHelper.startPage(dictDataVo.getPageNum(), dictDataVo.getPageSize());
        return dictDataMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public void insertDictData(DictData dictData) {
        dictDataMapper.insert(dictData);
    }

    @Override
    public void deleteDictData(DictDataVo dictDataVo) {
        dictDataMapper.deleteById(dictDataVo.getId());
    }

    @Override
    public void updateDictData(DictData dictData) {
        dictDataMapper.updateById(dictData);
    }

    @Override
    public DictData selectDictDataById(DictDataVo dictDataVo) {
        return dictDataMapper.selectById(dictDataVo.getId());
    }
}
