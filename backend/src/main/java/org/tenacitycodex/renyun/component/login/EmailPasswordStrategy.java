package org.tenacitycodex.renyun.component.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.common.exceptions.ApiException;
import org.tenacitycodex.renyun.component.BaseLoginStrategy;
import org.tenacitycodex.renyun.module.user.entity.User;
import org.tenacitycodex.renyun.module.user.service.UserService;

import java.util.Map;

@Service
public class EmailPasswordStrategy extends BaseLoginStrategy {

    @Autowired
    private UserService userService;

    @Override
    public String getType() {
        return "EMAIL_PWD";
    }

    @Override
    protected String extractIdentityKey(Map<String, Object> params) {
        return String.valueOf(params.get("email"));
    }

    @Override
    protected User doAuthenticate(Map<String, Object> params) {
        String email = (String) params.get("email");
        String password = (String) params.get("password");

        if (email == null || email.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "邮箱不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "密码不能为空");
        }
        User user = userService.loginViaEmailPwd(email, password);

        if (user == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "邮箱或密码错误");
        }
        return user;
    }
}
