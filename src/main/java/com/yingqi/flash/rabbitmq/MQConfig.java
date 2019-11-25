package com.yingqi.flash.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * 配置消息交换机
 * 针对消费者配置
 FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念
 HeadersExchange ：通过添加属性key-value匹配
 DirectExchange:按照routingkey分发到指定队列
 TopicExchange:多关键字匹配
 **/
@Configuration
public class MQConfig {
    public static final String FLASH_QUEUE = "flash.queue";
    public static final String QUEUE = "queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADERS_QUEUE = "headers.queue2";
    public static final String TOPIC_EXCHAGE = "topic.exchage";
    public static final String FANOUT_EXCHAGE = "fanout.exchage";
    public static final String HEADERS_EXCHAGE = "headers.exchage";
    public static final String ROUTING_KEY1 = "topic.key1";
    public static final String ROUTING_KEY2 = "topic.#";

    //Direct模式 交换机模式 exchange
    @Bean
    public Queue quese() {
        return new Queue(QUEUE, true);
    }    @Bean
    public Queue flashQuese() {
        return new Queue(FLASH_QUEUE, true);
    }
    //Fanout 模式
    @Bean
    public FanoutExchange fanoutExchange() {
        return  new FanoutExchange(FANOUT_EXCHAGE);
    }
    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
    }
    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
    }
    //topic模式 交换机模式 exchange
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }
    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }
    @Bean
    public TopicExchange topicExchange() {
        return  new TopicExchange(TOPIC_EXCHAGE);
    }
    @Bean
    public Binding topicBinding1() {
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
    }
    @Bean
    public Binding topicBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
    }

    //Header模式
    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(HEADERS_EXCHAGE);
    }
    @Bean
    public Queue queueHeaders() {
        return new Queue(HEADERS_QUEUE);
    }
    @Bean
    public Binding headersBinding() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", "123321");
        return BindingBuilder.bind(queueHeaders()).to(headersExchange()).whereAll(map).match();
    }
}
