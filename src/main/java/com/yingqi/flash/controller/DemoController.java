package com.yingqi.flash.controller;

import com.yingqi.flash.domain.User;
import com.yingqi.flash.rabbitmq.MQSender;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.redis.UserKey;
import com.yingqi.flash.result.CodeMsg;
import com.yingqi.flash.result.Result;
import com.yingqi.flash.service.UserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    UserService userService;
    @Autowired
    RedisService redisService;
    @Autowired
    MQSender sender;

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq(){
        sender.send("hello,flash");
        return Result.success("hello,world");
    }
    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> mqTopic(){
        sender.sendTopic("hello,flash");
        return Result.success("hello,topic");
    }
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> mqFanout(){
        sender.sendFanout("hello,flash");
        return Result.success("hello,fanout");
    }
    @RequestMapping("/mq/headers")
    @ResponseBody
    public Result<String> mqHeaders(@RequestParam(value = "key",defaultValue = "key")String key,@RequestParam(value = "val",defaultValue = "000000")String val){
        sender.sendHeaders("hello,flash",key,val);
        return Result.success("hello,headers");
        //http://localhost:8080/demo/mq/headers?key=key&val=123321
    }


    @RequestMapping("/")
    @ResponseBody
    public String home() {
        return "Hello World!!";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello() {
        return Result.success("hello,ying");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError() {
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model) {
        model.addAttribute("name", "yingqi");
        return "hello";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> doget() {
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/insert")
    @ResponseBody
    public Result<User> doinsert() {
        User user = new User(2, "sss");
        userService.insertUser(user);
        return Result.success(user);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<String> redisGet() {
        String value = redisService.get(UserKey.getById(), "key1", String.class);
        return Result.success(value);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> reidsSet() {
        boolean value = redisService.set(UserKey.getById(), "key1", "hello,yingqi");
        return Result.success(value);
    }


}
