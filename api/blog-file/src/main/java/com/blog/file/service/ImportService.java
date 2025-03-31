package com.blog.file.service;


import com.blog.core.domain.file.files.vo.ImportDiaryVo;

public interface ImportService {

    boolean importDiary(ImportDiaryVo importDiaryVo);
}
