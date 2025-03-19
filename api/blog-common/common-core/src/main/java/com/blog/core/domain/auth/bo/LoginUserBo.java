package com.blog.core.domain.auth.bo;

import com.blog.core.domain.auth.entity.Menu;
import com.blog.core.domain.auth.entity.User;
import com.blog.core.domain.auth.vo.MenuVo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginUserBo extends User {

    private List<Menu> menu;
}
