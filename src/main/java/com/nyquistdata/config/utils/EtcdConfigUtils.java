package com.nyquistdata.config.utils;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 */
public class EtcdConfigUtils {
    private EtcdConfigUtils(){}

    public static String selectiveConvertUnicode(String configValue) {
        StringBuilder sb = new StringBuilder();
        char[] chars = configValue.toCharArray();
        for (char aChar : chars) {
            if (isBaseLetter(aChar)) {
                sb.append(aChar);
            }
            else {
                sb.append(String.format("\\u%04x", (int) aChar));
            }
        }
        return sb.toString();
    }

    public static boolean isBaseLetter(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        return ub == Character.UnicodeBlock.BASIC_LATIN || Character.isWhitespace(ch);
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }
}
