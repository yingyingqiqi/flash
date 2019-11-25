package com.yingqi.flash.vo;

import com.yingqi.flash.domain.FlashUser;

public class GoodsDetailVo {
    private int flashStatus = 0;
    private int remainSeconds = 0;
    private GoodsVo goodsVo;
    private FlashUser user;

    public FlashUser getUser() {
        return user;
    }

    public void setUser(FlashUser user) {
        this.user = user;
    }

    public int getFlashStatus() {
        return flashStatus;
    }

    public void setFlashStatus(int flashStatus) {
        this.flashStatus = flashStatus;
    }

    public int getRemainSeconds() {
        return remainSeconds;
    }

    public void setRemainSeconds(int remainSeconds) {
        this.remainSeconds = remainSeconds;
    }

    public GoodsVo getGoodsVo() {
        return goodsVo;
    }

    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }
}
