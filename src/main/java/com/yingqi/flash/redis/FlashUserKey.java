package com.yingqi.flash.redis;

public class FlashUserKey extends BasePrefix {

    private static final int TOKEN_EXPIRE = 3600*24*2;

    public static FlashUserKey token = new FlashUserKey(TOKEN_EXPIRE,"tk");
    public static FlashUserKey getByName = new FlashUserKey(TOKEN_EXPIRE,"name");
    public static FlashUserKey getById = new FlashUserKey(0,"id");

    public FlashUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
