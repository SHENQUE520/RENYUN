package org.tenacitycodex.renyun.component.login;

public class DoctorLoginStrategy extends UsernamePasswordStrategy {

    @Override
    public String getType() {
        return "doctor";
    }

    @Override
    protected String expectedRole() {
        return "doctor";
    }
}
