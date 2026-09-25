package org.tenacitycodex.renyun.component;

import org.springframework.stereotype.Component;
import org.tenacitycodex.renyun.module.user.entity.User;

import java.util.Map;

@Component
public interface ILoginStrategy {
    String getType();
    User authenticate(Map<String, Object> params) throws IllegalArgumentException;
}
