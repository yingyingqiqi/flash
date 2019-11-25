package com.yingqi.flash.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.yingqi.flash.redis.RedisService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * 配置消息交换机
 * 针对消费者配置
 FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念
 HeadersExchange ：通过添加属性key-value匹配
 DirectExchange:按照routingkey分发到指定队列
 TopicExchange:多关键字匹配
**/
@Service
public class MQSender {

    @Autowired
    AmqpTemplate amqpTemplate;

    public void send(Object message) {
        String msg = JSON.toJSONString(message);
        System.out.println(msg);
        amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
    }
    //topic模式 交换机模式 exchange
    public void sendTopic(Object message) {
        String msg = RedisService.beanToString(message);
        System.out.println("send+topic模式"+msg+new Date());
        //发两条不同key消息，接收者收到3条
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHAGE,"topic.key1",msg+"topic.key1"+new Date());//发送的时候topicQueue1和2都接收到了
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHAGE,"topic.key2",msg+"topic.key2"+new Date());
    }
    //Fanout 模式
    public void sendFanout(Object message) {
        String msg = RedisService.beanToString(message);
        System.out.println("send+Fanout 模式:"+msg+new Date());
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHAGE,"",msg+new Date());
    }
    //Header模式
    public void sendHeaders(Object message, String key ,String val) {
        String msg = RedisService.beanToString(message);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader(key,val);
        Message s = new Message(msg.getBytes(),messageProperties);
        System.out.println("send+Header模式:"+s.toString()+new Date()+"请验证，是否提交队列");
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHAGE,"",s);
    }


    //---------------------------上面是四种消息队列模式--------------------
    public void sendFlashMessage(FlashMessage fm) {
        String msg = RedisService.beanToString(fm);
        System.out.println("send message:"+msg);
        amqpTemplate.convertAndSend(MQConfig.FLASH_QUEUE,msg);
    }
}
