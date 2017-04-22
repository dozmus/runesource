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

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A collection of miscellaneous utility methods and constants.
 *
 * @author blakeman8192
 */
public class Misc {

    public static final int LOGIN_RESPONSE_OK = 2;
    public static final int LOGIN_RESPONSE_INVALID_CREDENTIALS = 3;
    public static final int LOGIN_RESPONSE_ACCOUNT_DISABLED = 4;
    public static final int LOGIN_RESPONSE_ACCOUNT_ONLINE = 5;
    public static final int LOGIN_RESPONSE_UPDATED = 6;
    public static final int LOGIN_RESPONSE_WORLD_FULL = 7;
    public static final int LOGIN_RESPONSE_LOGIN_SERVER_OFFLINE = 8;
    public static final int LOGIN_RESPONSE_LOGIN_LIMIT_EXCEEDED = 9;
    public static final int LOGIN_RESPONSE_BAD_SESSION_ID = 10;
    public static final int LOGIN_RESPONSE_PLEASE_TRY_AGAIN = 11;
    public static final int LOGIN_RESPONSE_NEED_MEMBERS = 12;
    public static final int LOGIN_RESPONSE_COULD_NOT_COMPLETE_LOGIN = 13;
    public static final int LOGIN_RESPONSE_SERVER_BEING_UPDATED = 14;
    public static final int LOGIN_RESPONSE_LOGIN_ATTEMPTS_EXCEEDED = 16;
    public static final int LOGIN_RESPONSE_MEMBERS_ONLY_AREA = 17;

    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;

    private static final char[] VALID_PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ".toCharArray();
    private static final char[] VALID_USERNAME_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789! ".toCharArray();
    private static final char[] VALID_CHARACTERS = {
            '_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9'
    };

    public static boolean validatePassword(String password) {
        // Length check
        if (password.length() == 0 || password.length() > 16) {
            return false;
        }

        // Checking each character
        for (int i = 0; i < password.length(); i++) {
            if (!contains(VALID_PASSWORD_CHARACTERS, password.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean validateUsername(String username) {
        // Length check
        if (username.length() == 0 || username.length() > 12) {
            return false;
        }

        // Checking each character
        for (int i = 0; i < username.length(); i++) {
            if (!contains(VALID_USERNAME_CHARACTERS, username.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean contains(char[] src, char key) {
        for (char c : src) {
            if (c == key) {
                return true;
            }
        }
        return false;
    }

    public static boolean validateColors(int[] colors) {
        if (!in(colors[0], 0, 11))
            return false;
        if (!in(colors[1], 0, 15))
            return false;
        if (!in(colors[2], 0, 15))
            return false;
        if (!in(colors[3], 0, 5))
            return false;
        if (!in(colors[4], 0, 7))
            return false;
        return true;
    }

    public static boolean validateAppearance(int[] appearance) {
        if (!in(appearance[0], 0, 8))
            return false;
        if (!in(appearance[1], 10, 17))
            return false;
        if (!in(appearance[2], 18, 25))
            return false;
        if (!in(appearance[3], 26, 31))
            return false;
        if (!in(appearance[4], 33, 34))
            return false;
        if (!in(appearance[5], 36, 40))
            return false;
        if (!in(appearance[6], 42, 43))
            return false;
        return true;
    }

    /**
     * Inclusive bounds check.
     */
    private static boolean in(int k, int a, int b) {
        return k >= a && k <= b;
    }

    /**
     * Generates the SHA256 hash for the given input.
     *
     * @param input input
     * @return hash of input (or null if an exception occurred)
     */
    public static String hashSha256(String input) {
        try {
            // Hashing input
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes("UTF-8"));
            byte[] digest = md.digest();

            // Constructing output
            StringBuilder builder = new StringBuilder();

            for (byte b : digest) {
                builder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
        }
        return null;
    }

    /**
     * Converts the username to a long value.
     */
    public static long encodeBase37(String name) {
        long l = 0L;

        for (int i = 0; i < name.length() && i < 12; i++) {
            char c = name.charAt(i);
            l *= 37L;

            if (c >= 'A' && c <= 'Z')
                l += (1 + c) - 65;
            else if (c >= 'a' && c <= 'z')
                l += (1 + c) - 97;
            else if (c >= '0' && c <= '9')
                l += (27 + c) - 48;
        }

        while (l % 37L == 0L && l != 0L)
            l /= 37L;
        return l;
    }

    /**
     * Converts the long into a username.
     */
    public static String decodeBase37(long name) throws IllegalArgumentException {
        try {
            if (name <= 0L || name >= 0x5b5b57f8a98a5dd1L) {
                throw new IllegalArgumentException();
            }

            if (name % 37L == 0L) {
                throw new IllegalArgumentException();
            }
            int i = 0;
            char ac[] = new char[12];

            while (name != 0L) {
                long l1 = name;
                name /= 37L;
                ac[11 - i++] = VALID_CHARACTERS[(int) (l1 - name * 37L)];
            }
            return new String(ac, 12 - i, i);
        } catch (RuntimeException ignored) {
        }
        throw new IllegalArgumentException();
    }

    /**
     * Source: https://stackoverflow.com/a/3001879
     */
    public static boolean willAdditionOverflow(int left, int right) {
        if (right < 0 && right != Integer.MIN_VALUE) {
            return willSubtractionOverflow(left, -right);
        } else {
            return (~(left ^ right) & (left ^ (left + right))) < 0;
        }
    }

    /**
     * Source: https://stackoverflow.com/a/3001879
     */
    public static boolean willSubtractionOverflow(int left, int right) {
        if (right < 0) {
            return willAdditionOverflow(left, -right);
        } else {
            return ((left ^ right) & (left ^ (left - right))) < 0;
        }
    }

    /**
     * A simple logging utility that prefixes all messages with a timestamp.
     *
     * @author blakeman8192
     */
    public static class TimestampLogger extends PrintStream {

        private BufferedWriter writer;
        private final DateFormat df = new SimpleDateFormat();

        /**
         * The OutputStream to log to.
         *
         * @param out
         */
        public TimestampLogger(OutputStream out, String file) throws IOException {
            super(out);
            writer = new BufferedWriter(new FileWriter(file, true));
        }

        @Override
        public void println(String msg) {
            msg = "[" + df.format(new Date()) + "]: " + msg;
            super.println(msg);
            log(msg);
        }

        /**
         * Logs the message to the log file.
         *
         * @param msg the message
         */
        private void log(String msg) {
            try {
                writer.write(msg);
                writer.newLine();
                writer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    /**
     * A simple timing utility.
     *
     * @author blakeman8192
     */
    public static class Stopwatch {

        /**
         * The cached time.
         */
        private long time = System.currentTimeMillis();

        /**
         * Resets this stopwatch.
         */
        public void reset() {
            time = System.currentTimeMillis();
        }

        /**
         * Returns the amount of time elapsed (in milliseconds) since this
         * object was initialized, or since the last call to the "reset()"
         * method.
         *
         * @return the elapsed time (in milliseconds)
         */
        public long elapsed() {
            return System.currentTimeMillis() - time;
        }

    }

}
