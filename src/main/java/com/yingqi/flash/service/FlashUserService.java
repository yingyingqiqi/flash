package com.yingqi.flash.service;

import com.sun.org.apache.bcel.internal.classfile.Code;
import com.yingqi.flash.dao.FlashUserDao;
import com.yingqi.flash.domain.FlashUser;
import com.yingqi.flash.exception.GlobalException;
import com.yingqi.flash.redis.FlashUserKey;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.result.CodeMsg;
import com.yingqi.flash.util.MD5Utils;
import com.yingqi.flash.util.UUIDUtil;
import com.yingqi.flash.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class FlashUserService {
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired(required = false)
    FlashUserDao flashUserDao;

    @Autowired
    RedisService redisService;

    public FlashUser getById(Long id) {
        //从缓存里面取
        FlashUser user = redisService.get(FlashUserKey.getById, "" + id, FlashUser.class);
        if (user != null) {
            return user;
        }
            //从数据库取
         user = flashUserDao.getById(id);
        if (user != null) {
            redisService.set(FlashUserKey.getById, "" + id, user);
        }
        return user;
    }

    public boolean updatePassword(Long id,String passwordNow) {
        //取user
        FlashUser user = getById(id);
        if (user == null) {
            throw new GlobalException(CodeMsg.PASSWORD_EMPTY);
        }
        //更新数据库
        FlashUser flashUser = new FlashUser();
        flashUser.setId(user.getId());
        flashUser.setPassword(MD5Utils.formPassToDbPass(passwordNow,user.getSalt()));
        flashUserDao.update(flashUser);
        //防止下面程序调用出现异常，更新缓存
        redisService.delete(FlashUserKey.getById, "" + id);
        user.setPassword(flashUser.getPassword());
        redisService.set(FlashUserKey.token, "", user);
        return true;
    }

    public String login(HttpServletResponse response,LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formpass = loginVo.getPassword();
        //判读手机号是否存在
        FlashUser user = flashUserDao.getById(Long.valueOf(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
//            return CodeMsg.MOBILE_NOT_EXIST;
        }
        //验证密码
        String dbPass = user.getPassword();
        String slatDB = user.getSalt();
        String calcPass = MD5Utils.formPassToDbPass(formpass, slatDB);
        if (!calcPass.equals(dbPass)) {
//            return CodeMsg.PASSWORD_ERROR;
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成token
        String token = UUIDUtil.uuid();
        addCookie(response, user,token);
        return token;
    }


    public FlashUser getByToken(HttpServletResponse response,String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        //缓存关闭重启后，直接进入页面已token形式是拿不到用户信息的。
        FlashUser user = redisService.get(FlashUserKey.token, token, FlashUser.class);
        //延长有效期
        if (user != null) {
            addCookie(response,user,token);
        }
        return user;
    }

    private void addCookie(HttpServletResponse response, FlashUser user,String token) {
        //生成cookie
        redisService.set(FlashUserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(FlashUserKey.token.expireSecondes());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
