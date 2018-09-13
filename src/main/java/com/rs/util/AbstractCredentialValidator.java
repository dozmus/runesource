package com.rs.util;

public abstract class AbstractCredentialValidator {

    public boolean validate(String username, String password) {
        return validateUsername(username) && validatePassword(password);
    }

    public abstract boolean validateUsername(String username);

    public abstract boolean validatePassword(String password);
}
