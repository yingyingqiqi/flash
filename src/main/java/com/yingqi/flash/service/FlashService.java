package com.yingqi.flash.service;

import com.yingqi.flash.dao.GoodsDao;
import com.yingqi.flash.dao.OrderDao;
import com.yingqi.flash.domain.FlashOrder;
import com.yingqi.flash.domain.FlashUser;
import com.yingqi.flash.domain.Goods;
import com.yingqi.flash.domain.OrderInfo;
import com.yingqi.flash.redis.FlashKey;
import com.yingqi.flash.redis.GoodsKey;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.util.MD5Utils;
import com.yingqi.flash.util.UUIDUtil;
import com.yingqi.flash.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
public class FlashService {
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    @Autowired
    RedisService redisService;

    @Transactional
    public OrderInfo flash(FlashUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单
        boolean success = goodsService.reduceStock(goods);//添加一个 stock_count > 0
        if (success) {
            return orderService.createOrder(user, goods); //添加唯一索引
        }
       return null;
    }

    public Long getFlashResult(Long userId, long goodsId) {
        FlashOrder order = orderService.getFlashOrderByUserldGoodsid(userId, goodsId);
        if (order != null) {//成功
            return order.getOrderId();
        } else {
            Integer count = redisService.get(GoodsKey.getFlashGoodsStock, "" + goodsId, Integer.class);
            if (count > 0) {
                return Long.valueOf(0);
            }else{
                return Long.valueOf(1);
            }
        }
    }

    public String createFlashPath(FlashUser user, long goodsId) {
        String str = MD5Utils.md5(UUIDUtil.uuid() + "123456");
        redisService.set(FlashKey.getFlashPath, "" + user.getId() + "_" + goodsId, str);
        return str;
    }

    public boolean checkPath(FlashUser user, long goodsId, String path) {
        if (user == null || path == null) {
            return false;
        }
        String str = redisService.get(FlashKey.getFlashPath, "" + user.getId() + "_" + goodsId, String.class);
        return str.equals(path);
    }

    public BufferedImage createVerifyCode(FlashUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(FlashKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    public boolean checkVerifyCode(FlashUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) {
            return false;
        }
        Integer codeOld = redisService.get(FlashKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
        if(codeOld == null || codeOld - verifyCode != 0 ) {
            return false;
        }
        redisService.delete(FlashKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
        return true;
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * + - *
     * */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }
}
