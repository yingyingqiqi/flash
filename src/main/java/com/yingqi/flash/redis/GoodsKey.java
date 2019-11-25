package com.yingqi.flash.redis;

public class GoodsKey extends BasePrefix {
    public GoodsKey(int expireSeconds,String prefix) {
        super(expireSeconds,prefix);
    }

    public static GoodsKey getGoodsList = new GoodsKey(60,"gl");
    public static GoodsKey getGoodsDetail = new GoodsKey(60,"gd");
    public static GoodsKey getGoodsDetailById = new GoodsKey(60,"gi");
    public static GoodsKey getFlashGoodsStock = new GoodsKey(0,"gs");
}
