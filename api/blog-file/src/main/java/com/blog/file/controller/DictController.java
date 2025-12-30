package com.blog.file.controller;

import com.blog.core.domain.file.dict.DictData;
import com.blog.core.domain.file.dict.DictType;
import com.blog.core.domain.file.dict.vo.DictDataVo;
import com.blog.core.domain.file.dict.vo.DictTypeVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.service.DictService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 字典表接口
 * @Author lxk
 * @CreateTime 2025-12-25
 */

@RestController
@RequestMapping("/dict")
public class DictController {

    @Resource
    private DictService dictService;


    @GetMapping("/type/list")
    public Result selectDictTypeList(DictTypeVo dictTypeVo) {
        return ResultFactory.buildSuccessResult(dictService.selectDictTypeList(dictTypeVo));
    }

    @PostMapping("/type/insert")
    public Result insertDictType(@RequestBody DictType dictType) {
        dictService.insertDictType(dictType);
        return ResultFactory.buildSuccessResult();
    }

    @DeleteMapping("/type/delete")
    public Result deleteDictType(DictTypeVo dictTypeVo) {
        dictService.deleteDictType(dictTypeVo);
        return ResultFactory.buildSuccessResult();
    }

    @PostMapping("/type/update")
    public Result updateDictType(@RequestBody DictType dictType) {
        dictService.updateDictType(dictType);
        return ResultFactory.buildSuccessResult();
    }

    @GetMapping("/type/select")
    public Result selectDictTypeById(DictTypeVo dictTypeVo) {
        return ResultFactory.buildSuccessResult(dictService.selectDictTypeById(dictTypeVo));
    }

    @GetMapping("/data/list")
    public Result selectDictDataList(DictDataVo dictDataVo) {
        return ResultFactory.buildSuccessResult(dictService.selectDictDataList(dictDataVo));
    }

    @PostMapping("/data/insert")
    public Result insertDictData(@RequestBody DictData dictData) {
        dictService.insertDictData(dictData);
        return ResultFactory.buildSuccessResult();
    }

    @DeleteMapping("/data/delete")
    public Result deleteDictData(DictDataVo dictDataVo) {
        dictService.deleteDictData(dictDataVo);
        return ResultFactory.buildSuccessResult();
    }

    @PostMapping("/data/update")
    public Result updateDictData(@RequestBody DictData dictData) {
        dictService.updateDictData(dictData);
        return ResultFactory.buildSuccessResult();
    }

    @GetMapping("/data/select")
    public Result selectDictDataById(DictDataVo dictDataVo) {
        return ResultFactory.buildSuccessResult(dictService.selectDictDataById(dictDataVo));
    }
}
