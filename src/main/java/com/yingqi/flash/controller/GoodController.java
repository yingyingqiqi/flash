package com.yingqi.flash.controller;

import com.yingqi.flash.domain.FlashUser;
import com.yingqi.flash.redis.GoodsKey;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.result.Result;
import com.yingqi.flash.service.FlashUserService;
import com.yingqi.flash.service.GoodsService;
import com.yingqi.flash.vo.GoodsDetailVo;
import com.yingqi.flash.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/goods")
public class GoodController {
    //？？？？
    private static Logger log = LoggerFactory.getLogger(GoodController.class);

    @Autowired
    FlashUserService flashUserService;
    @Autowired
    RedisService redisService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;
    @Autowired
    ApplicationContext applicationContext ;

//    @RequestMapping("/to_list")
//    public String toList(
//                           HttpServletResponse response,
//            Model model,
//                          @CookieValue(value = FlashUserService.COOKIE_NAME_TOKEN, required = false) String cookieToken,
//                          @RequestParam(value = FlashUserService.COOKIE_NAME_TOKEN, required = false) String paramToken) {
//                            FlashUser user){
//        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
//            return "login";
//        }
//        String token = StringUtils.isEmpty(cookieToken) ? paramToken : cookieToken;
//        FlashUser flashUser = flashUserService.getByToken(response,token);
//        model.addAttribute("user", user);
//        return "goods_list";
//    }
//QPS 500 1000-*10次
/*  页面缓存优化前
    @RequestMapping("/to_list")
    public String toList(Model model, FlashUser user) {
        model.addAttribute("user", user);
        //查询商品
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
//        System.out.println(goodsList);
        model.addAttribute("goodsList",goodsList);
        return "goods_list";
    }*/
   // 页面缓存优化后 QPS 1300 1000-*10次
    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toList( HttpServletRequest request,
                          HttpServletResponse response,
                          Model model) {
        //从缓存中取出来
        String html = redisService.get(GoodsKey.getGoodsList,"", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        //查询商品
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);
//        return "goods_list";
        //手动渲染
        SpringWebContext context = new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }
    @RequestMapping(value = "/to_detail/{goodsid}",produces = "text/html")
    @ResponseBody
    public String toDetail(HttpServletRequest request,
                           HttpServletResponse response,
                           Model model,
                           FlashUser user,
                           @PathVariable("goodsid")long goodsId) {
        model.addAttribute("user", user);
        //从缓存中取出来
        String html = redisService.get(GoodsKey.getGoodsDetail,""+goodsId, String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        //查询商品
        GoodsVo goods = goodsService.getGoodsVoByGoodId(goodsId);
        //
        long startAt = goods.getStartDate().getTime()-28800*1000;//数据库版本和依赖问题，可以通过api解决
        long entAt = goods.getEndDate().getTime()-28800*1000;
        long now  = System.currentTimeMillis();
        int flashStatus = 0 ;
        int remainSeconds = 0 ;
        if (now < startAt) {//未开始
            flashStatus = 0 ;
            remainSeconds = (int) ((startAt - now) / 1000);
            System.out.println(remainSeconds);
        } else if (now > entAt) {//已结束
            flashStatus = 2;
            remainSeconds=-1;
        }else{//进行中
            flashStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("flashStatus",flashStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("goods",goods);

        //手动渲染
        SpringWebContext context = new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", context);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, ""+goodsId, html);
        }
        return html;
//        return "goods_detail";
    }

    //页面静态化优化，浏览器缓存
    @RequestMapping(value = "/detail2/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> toDetail2(HttpServletRequest request,
                                           HttpServletResponse response,
                                           Model model,
                                           FlashUser user,
                                           @PathVariable("goodsId")long goodsId) {
        //查询商品
        GoodsVo goods = goodsService.getGoodsVoByGoodId(goodsId);
        //
        long startAt = goods.getStartDate().getTime()-28800*1000;//数据库版本和依赖问题，可以通过api解决
        long entAt = goods.getEndDate().getTime()-28800*1000;
        long now  = System.currentTimeMillis();
        int flashStatus = 0 ;
        int remainSeconds = 0 ;
        if (now < startAt) {//未开始
            flashStatus = 0 ;
            remainSeconds = (int) ((startAt - now) / 1000);
            System.out.println(remainSeconds);
        } else if (now > entAt) {//已结束
            flashStatus = 2;
            remainSeconds=-1;
        }else{//进行中
            flashStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoodsVo(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setFlashStatus(flashStatus);
        return Result.success(vo);
//        return "goods_detail";
    }
}
