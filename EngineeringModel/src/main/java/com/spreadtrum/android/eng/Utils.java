package com.spreadtrum.android.eng;

import java.util.regex.Pattern;

public class Utils {
    public static int parseInt(String str) {
        if (str == null || "".equals(str)) {
            return 0;
        }
        return Integer.parseInt(str);
    }

    public static boolean isInt(String str) {
        if (Pattern.compile("^-?\\d+").matcher(str).matches()) {
            return true;
        }
        return false;
    }
}
