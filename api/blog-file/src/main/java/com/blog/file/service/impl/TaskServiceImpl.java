package com.blog.file.service.impl;

import com.blog.core.domain.file.task.vo.TaskInfoVo;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.file.mapper.TaskInfoMapper;
import com.blog.file.mapper.TaskLogMapper;
import com.blog.file.service.TaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 任务服务实现类
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskInfoMapper taskInfoMapper;

    @Resource
    private TaskLogMapper taskLogMapper;

    @Override
    public void insertTask(TaskInfoVo taskInfoVo) {
        taskInfoMapper.insert(taskInfoVo);
    }

    @Override
    public void updateTask(TaskInfoVo taskInfoVo) {
        taskInfoMapper.updateById(taskInfoVo);
    }

    @Override
    public void deleteTask(Integer id) {
        taskInfoMapper.deleteById(id);
    }

    @Override
    public List<TaskInfoVo> selectTaskInfoList() {
        return List.of();
    }

    @Override
    public List<TaskLogVo> selectTaskLogList() {
        return List.of();
    }
}
