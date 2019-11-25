package com.yingqi.flash.controller;

import com.sun.org.apache.bcel.internal.generic.MONITORENTER;
import com.yingqi.flash.access.AccessLimit;
import com.yingqi.flash.domain.FlashGoods;
import com.yingqi.flash.domain.FlashOrder;
import com.yingqi.flash.domain.FlashUser;
import com.yingqi.flash.domain.OrderInfo;
import com.yingqi.flash.rabbitmq.FlashMessage;
import com.yingqi.flash.rabbitmq.MQSender;
import com.yingqi.flash.redis.FlashKey;
import com.yingqi.flash.redis.GoodsKey;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.result.CodeMsg;
import com.yingqi.flash.result.Result;
import com.yingqi.flash.service.FlashService;
import com.yingqi.flash.service.FlashUserService;
import com.yingqi.flash.service.GoodsService;
import com.yingqi.flash.service.OrderService;
import com.yingqi.flash.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/flash")
public class FlashController implements InitializingBean {

    private static Logger log = LoggerFactory.getLogger(FlashController.class);
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
    @Autowired
    MQSender sender;
    Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    //系统初始化，系统初始化，把商品库存数量加载到redis
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        if (goodsVos == null) {
            return;
        }
        for (GoodsVo goods : goodsVos) {
            redisService.set(GoodsKey.getFlashGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    //QPS 760   1000*10次
    //新 QPS 901   1000*10次   加了redis
    //新 QPS 1986   1000*10次  加了mq和内存标记
    //新                        隐藏秒杀地址
    @RequestMapping(value = "/{path}/do_flash", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> toFlash(Model model,
                                   FlashUser user,
                                   @RequestParam("goodsId") long goodsId,
                                   @PathVariable("path") String path) {
        model.addAttribute("user", user);
        System.out.println(path);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = flashService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记，减少redis访问。
        Boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //判断库存
        Long decr = redisService.decr(GoodsKey.getFlashGoodsStock, "" + goodsId);
        if (decr < 0) {
            localOverMap.put(goodsId, true);//后面加的
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //判断是否已经秒杀到了
        FlashOrder order = orderService.getFlashOrderByUserldGoodsid(user.getId(), goodsId);
        if (order != null) {
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA);
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //入队sender
        FlashMessage fm = new FlashMessage();
        fm.setUser(user);
        fm.setGoodId(goodsId);
        sender.sendFlashMessage(fm);
        return Result.success(0);
      /*  //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodId(goodsid);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        FlashOrder order = orderService.getFlashOrderByUserldGoodsid(user.getId(), goodsId);
        if (order != null) {
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA);
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = flashService.flash(user, goods);
        return Result.success(orderInfo);*/
    }

    /**
     * orderId :成功
     * 1：秒杀失败
     * 0：排队中
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> result(Model model, FlashUser user, @RequestParam("goodsId") long goodsId) {

        model.addAttribute("user", user);
        //判断库存, 可以删除，使用localOverMap代替
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        Long result = flashService.getFlashResult(user.getId(), goodsId);
        return Result.success(result);
    }
    //隐藏秒杀地址
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path")
    @ResponseBody
    public Result<String> toPath(Model model, FlashUser user, @RequestParam("goodsId") long goodsId,
                                 @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //检查验证码
        boolean check = flashService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = flashService.createFlashPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> toVerifyCode(HttpServletResponse response, Model model,
                                       FlashUser user, @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image  = flashService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }



}
