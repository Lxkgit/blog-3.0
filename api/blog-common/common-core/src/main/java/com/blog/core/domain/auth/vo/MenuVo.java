package com.blog.core.domain.auth.vo;

import com.blog.core.domain.auth.entity.Menu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description 菜单Vo类
 * @Author lxk
 * @CreateTime 2025-03-05
 */

@Getter
@Setter
public class MenuVo extends Menu {

    //子集菜单
    private List<Menu> children;
}
