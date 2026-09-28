package org.nezxenka.auth.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Hex {

    private final char[] DIGITS = "0123456789abcdef".toCharArray();

    public String encode(byte[] bytes) {
        char[] result = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            result[i * 2] = DIGITS[value >>> 4];
            result[i * 2 + 1] = DIGITS[value & 0x0F];
        }
        return new String(result);
    }
}
