package com.rs.util;

/**
 * A lenient credential validator, which enforces a character whitelist, as well as length
 * limits on both username and passwords.
 */
public class LenientCredentialValidator extends AbstractCredentialValidator {

    private static final char[] VALID_PASSWORD_CHARACTERS = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            + "0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ").toCharArray();
    private static final char[] VALID_USERNAME_CHARACTERS = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            + "0123456789! ").toCharArray();

    public boolean validateUsername(String username) {
        // Length check
        if (username.length() == 0 || username.length() > 12) {
            return false;
        }

        // Checking each character
        for (int i = 0; i < username.length(); i++) {
            if (!Misc.contains(VALID_USERNAME_CHARACTERS, username.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean validatePassword(String password) {
        // Length check
        if (password.length() == 0 || password.length() > 20) {
            return false;
        }

        // Checking each character
        for (int i = 0; i < password.length(); i++) {
            if (!Misc.contains(VALID_PASSWORD_CHARACTERS, password.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
