package org.tenacitycodex.renyun.component.abstracts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.tenacitycodex.renyun.common.dto.request.RegisterRequest;
import org.tenacitycodex.renyun.module.user.entity.User;

public interface IUserService {
    @Transactional
    void saveUser(User user);
    User getUserById(Long id);
    User getUserByEmail(String email);
    Page<User> getAllUsers(Pageable page);
    User loginViaEmailPwd(String email, String password);
    User loginViaEmailValidation(String email, String code);

    User register(RegisterRequest payload, String password);
}
