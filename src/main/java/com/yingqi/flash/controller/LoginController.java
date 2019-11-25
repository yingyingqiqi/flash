package com.yingqi.flash.controller;

import com.sun.org.apache.bcel.internal.classfile.Code;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.result.CodeMsg;
import com.yingqi.flash.result.Result;
import com.yingqi.flash.service.FlashUserService;
import com.yingqi.flash.service.UserService;
import com.yingqi.flash.util.ValidatorUtil;
import com.yingqi.flash.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    //？？？？
    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    FlashUserService flashUserService;
    @Autowired
    RedisService redisService ;

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    //@Valid为标准JSR-303规范，
    // @Validated是spring封装好的实现(@Validated LoginVo LoginVo, BindingResult bindingResult，HttpServletResponse response)
    //BindingResult 为参数效验的结果。使用@Valid没有此参数。
    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
       /* //参数效验
        String passInput = loginVo.getPassword();
        String mobile = loginVo.getMobile();
        if (StringUtils.isEmpty(passInput)) {
            return Result.error(CodeMsg.PASSWORD_EMPTY);
        }
        if (StringUtils.isEmpty(mobile)) {
            return Result.error(CodeMsg.MOBILE_EMPTY);
        }
        if (!ValidatorUtil.isMobile(mobile)) {
            return Result.error(CodeMsg.MOBILE_ERROR);

        }
        //登陆
        CodeMsg login = flashUserService.login(loginVo);
        if (login.getCode() == 0) {
            return Result.success(CodeMsg.SUCCESS);
        }else{
            return Result.error(login);
        }*/
        //登陆
        String token = flashUserService.login(response, loginVo);
        return Result.success(token);
    }

}
