package com.yingqi.flash.rabbitmq;

import com.yingqi.flash.domain.FlashOrder;
import com.yingqi.flash.domain.FlashUser;
import com.yingqi.flash.domain.OrderInfo;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.result.CodeMsg;
import com.yingqi.flash.result.Result;
import com.yingqi.flash.service.FlashService;
import com.yingqi.flash.service.FlashUserService;
import com.yingqi.flash.service.GoodsService;
import com.yingqi.flash.service.OrderService;
import com.yingqi.flash.vo.GoodsVo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    @Autowired
    FlashUserService flashUserService;
    @Autowired
    RedisService redisService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    @Autowired
    FlashService flashService;

    /**
     * Direct模式 交换机模式 exchange
     * @param message
     */
    @RabbitListener(queues = MQConfig.QUEUE )
    public void receive(String message) {
        System.out.println(message);
    }
    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1 )
    public void receiveTopic1(String message) {
        System.out.println("Receiver:TOPIC_QUEUE1-"+message);
    }
    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2 )
    public void receiveTopic2(String message) {
        System.out.println("Receiver:TOPIC_QUEUE2-"+message);
    }
    @RabbitListener(queues = MQConfig.HEADERS_QUEUE )
    public void receiveHeaders(String message) {
        System.out.println("Receiver:HEADERS_QUEUE-"+message);
    }
    //---------------------------上面是四种消息队列模式--------------------

    @RabbitListener(queues = MQConfig.FLASH_QUEUE )
    public void receiveFlash(String message) {
        System.out.println("receive："+message);
        FlashMessage fm = RedisService.StringToBean(message, FlashMessage.class);
        FlashUser user = fm.getUser();
        long goodsId = fm.getGoodId();
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
           return;
        }
        //判断是否已经秒杀到了
        FlashOrder order = orderService.getFlashOrderByUserldGoodsid(user.getId(), goodsId);
        if (order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单
        flashService.flash(user, goods);
    }
}
