package com.yingqi.flash.redis;

public class UserKey extends BasePrefix {
    private UserKey(String prefix) {
        super(prefix);
    }

    private UserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static UserKey getById() {
        return new UserKey("id");
    }

    public static UserKey getByName() {
        return new UserKey("name");
    }
}
