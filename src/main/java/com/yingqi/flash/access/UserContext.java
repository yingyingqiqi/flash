package com.yingqi.flash.access;

import com.yingqi.flash.domain.FlashUser;

public class UserContext {
    private static ThreadLocal<FlashUser> userHolder = new ThreadLocal<FlashUser>();

    public static void setUserHolder(FlashUser user) {
        userHolder.set(user);
    }

    public static FlashUser getUserHolder() {
        return userHolder.get();
    }
}
