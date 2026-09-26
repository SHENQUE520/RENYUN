package org.tenacitycodex.renyun.component.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.common.exceptions.ApiException;
import org.tenacitycodex.renyun.component.BaseLoginStrategy;
import org.tenacitycodex.renyun.module.user.entity.User;
import org.tenacitycodex.renyun.module.user.service.UserService;

import java.util.Map;

@Component
public abstract class UsernamePasswordStrategy extends BaseLoginStrategy {

    @Autowired
    protected UserService userService;

    protected abstract String expectedRole();

    @Override
    protected String extractIdentityKey(Map<String, Object> params) {
        return String.valueOf(params.get("username"));
    }

    @Override
    protected User doAuthenticate(Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");

        if (username == null || username.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "密码不能为空");
        }
        User user = userService.loginViaUsernamePwd(username, password);

        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!expectedRole().equals(user.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "当前账号不属于该登录入口");
        }
        return user;
    }
}
