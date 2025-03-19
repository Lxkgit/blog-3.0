package com.blog.core.domain.auth.vo;

import com.blog.core.domain.auth.entity.Menu;
import com.blog.core.domain.auth.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserVo extends User {

    private List<Menu> menu;
}
