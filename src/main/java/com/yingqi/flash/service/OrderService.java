package com.yingqi.flash.service;

import com.yingqi.flash.dao.GoodsDao;
import com.yingqi.flash.dao.OrderDao;
import com.yingqi.flash.domain.FlashOrder;
import com.yingqi.flash.domain.FlashUser;
import com.yingqi.flash.domain.OrderInfo;
import com.yingqi.flash.redis.OrderKey;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {
    @Autowired(required = false)
    OrderDao orderDao;
    @Autowired
    RedisService redisService;
    public FlashOrder getFlashOrderByUserldGoodsid(Long userId, long goodsId) {
//        return orderDao.getFlashOrderByUserIdGoodsId(userId, goodsId);
        return redisService.get(OrderKey.getFlashOrderByUidGid, "" + userId + "_" + goodsId, FlashOrder.class);
    }

    @Transactional
    public OrderInfo createOrder(FlashUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getFlashPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
//        long orderId = orderDao.insert(orderInfo); //出现订单表相同ID
        orderDao.insert(orderInfo);
        FlashOrder flashOrder = new FlashOrder();
        flashOrder.setGoodsId(goods.getId());
        flashOrder.setOrderId(orderInfo.getId());
        flashOrder.setUserId(user.getId());
        orderDao.insertFlashOrder(flashOrder);

        redisService.set(OrderKey.getFlashOrderByUidGid, "" + user.getId() + "_" + goods.getId(),flashOrder );

        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }
}
