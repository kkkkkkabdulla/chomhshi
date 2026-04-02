package com.campus.util;

import java.util.UUID;

public class TokenUtil {

    private TokenUtil() {
    }

    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
