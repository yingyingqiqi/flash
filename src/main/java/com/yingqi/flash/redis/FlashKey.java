package com.yingqi.flash.redis;

public class FlashKey extends BasePrefix {
    public FlashKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static FlashKey getFlashPath = new FlashKey(60, "gp");
    public static FlashKey getMiaoshaVerifyCode = new FlashKey(3000, "gvc");
    public static FlashKey ACCESS = new FlashKey(5, "access");

    public static FlashKey ACCESSCOUNT(int count) {
        return new FlashKey(count, "accesscount");
    }
}
