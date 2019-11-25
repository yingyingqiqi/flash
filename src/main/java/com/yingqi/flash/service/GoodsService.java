package com.yingqi.flash.service;

import com.yingqi.flash.dao.GoodsDao;
import com.yingqi.flash.domain.FlashGoods;
import com.yingqi.flash.domain.Goods;
import com.yingqi.flash.redis.GoodsKey;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    @Autowired(required = false)
    GoodsDao goodsDao;
    @Autowired
    RedisService redisService;

    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodId(long goodsid) {
       /* GoodsVo vo = redisService.get(GoodsKey.getGoodsDetailById, "", GoodsVo.class);
        if (vo != null) {
            return vo;
        }
        GoodsVo goodsVoByGoodsId = goodsDao.getGoodsVoByGoodsId(goodsid);
        if (goodsVoByGoodsId != null) {
            redisService.set(GoodsKey.getGoodsDetailById, "", goodsVoByGoodsId);
        }
        return goodsVoByGoodsId;*/
       return goodsDao.getGoodsVoByGoodsId(goodsid);
    }

    public boolean reduceStock(GoodsVo goods) {
        FlashGoods g = new FlashGoods();
        g.setGoodsId(goods.getId());
        int ret = goodsDao.reduceStock(g);
        System.out.println(ret);
        return ret > 0;
    }
}
