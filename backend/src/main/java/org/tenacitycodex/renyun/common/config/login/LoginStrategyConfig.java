package org.tenacitycodex.renyun.common.config.login;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tenacitycodex.renyun.component.ILoginStrategy;
import org.tenacitycodex.renyun.component.login.DoctorLoginStrategy;
import org.tenacitycodex.renyun.component.login.PatientLoginStrategy;

@Configuration
public class LoginStrategyConfig {
    @Bean("PATIENT")
    public ILoginStrategy patientLogin() {
        return new PatientLoginStrategy();
    }

    @Bean("DOCTOR")
    public ILoginStrategy doctorLogin() {
        return new DoctorLoginStrategy();
    }
}
