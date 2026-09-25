package org.tenacitycodex.renyun.common.config.login;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tenacitycodex.renyun.component.ILoginStrategy;
import org.tenacitycodex.renyun.component.login.EmailPasswordStrategy;

@Configuration
public class LoginStrategyConfig {
    @Bean("EMAIL_PWD")
    public ILoginStrategy emailPasswordLogin(){
        return new EmailPasswordStrategy();
    }
}
