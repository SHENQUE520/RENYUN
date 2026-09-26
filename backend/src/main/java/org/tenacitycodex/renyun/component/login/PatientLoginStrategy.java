package org.tenacitycodex.renyun.component.login;

public class PatientLoginStrategy extends UsernamePasswordStrategy {

    @Override
    public String getType() {
        return "patient";
    }

    @Override
    protected String expectedRole() {
        return "patient";
    }
}
