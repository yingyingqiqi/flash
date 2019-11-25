package com.yingqi.flash.rabbitmq;

import com.yingqi.flash.domain.FlashUser;

public class FlashMessage {
    private FlashUser user;
    private long goodId;

    public FlashUser getUser() {
        return user;
    }

    public void setUser(FlashUser user) {
        this.user = user;
    }

    public long getGoodId() {
        return goodId;
    }

    public void setGoodId(long goodId) {
        this.goodId = goodId;
    }
}
