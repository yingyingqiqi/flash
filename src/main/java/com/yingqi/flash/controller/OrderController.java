package com.yingqi.flash.controller;

import com.yingqi.flash.domain.FlashUser;
import com.yingqi.flash.domain.OrderInfo;
import com.yingqi.flash.redis.GoodsKey;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.result.CodeMsg;
import com.yingqi.flash.result.Result;
import com.yingqi.flash.service.FlashUserService;
import com.yingqi.flash.service.GoodsService;
import com.yingqi.flash.service.OrderService;
import com.yingqi.flash.vo.GoodsDetailVo;
import com.yingqi.flash.vo.GoodsVo;
import com.yingqi.flash.vo.OrderDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    FlashUserService flashUserService;
    @Autowired
    RedisService redisService;
    @Autowired
    OrderService orderService;
    @Autowired
    GoodsService goodsService;
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, FlashUser user, @RequestParam("orderId")long orderId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order = orderService.getOrderById(orderId);
        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);
        return Result.success(vo);
    }


}
