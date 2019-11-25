package com.yingqi.flash.redis;

public interface KeyPrefix {
    public int expireSecondes();
    public String getPrefix();
}
