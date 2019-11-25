package com.yingqi.flash.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {
    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d";

    public static String inputPassFormPasss(String inputPass) {
        String str = ""+ salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    public static String formPassToDbPass(String fromPass,String salt) {
        String str = ""+ salt.charAt(0) + salt.charAt(2) + fromPass + salt.charAt(5) + salt.charAt(4);
//        System.out.println(md5(str));
        return md5(str);
    }

    //模拟数据库密码
    public static String inputPassToDbPass(String input, String saltDb) {
        String formPasss = inputPassFormPasss(input);
        String dbPass = formPassToDbPass(formPasss, saltDb);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(inputPassFormPasss("123456"));//d3b1294a61a07da9b49b6e22b2cbd7f9,彩虹表反查12123456c3
        System.out.println(formPassToDbPass(inputPassFormPasss("123456"),"1a2b3c4d"));//b7797cce01b4b131b433b6acf4add449,彩虹表反查xxx
        System.out.println(inputPassToDbPass(("123456"),"1a2b3c4d"));//b7797cce01b4b131b433b6acf4add449,彩虹表反查xxx
    }

}
