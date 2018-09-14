package com.rs.util;
/*
 * This file is part of RuneSource.
 *
 * RuneSource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RuneSource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RuneSource.  If not, see <http://www.gnu.org/licenses/>.
 */

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
