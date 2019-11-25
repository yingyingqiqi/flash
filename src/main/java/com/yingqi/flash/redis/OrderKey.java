package com.yingqi.flash.redis;

public class OrderKey extends BasePrefix {
    public OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static OrderKey getFlashOrderByUidGid = new OrderKey(0,"moug");
}
