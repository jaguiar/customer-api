package com.prez.utils;

public class StringUtils {

  public static String anonymize(String value, int offset) {
    int idx = Math.min(offset, value.length() / 2);
    return value.substring(0, idx) + "********************";
  }
}
