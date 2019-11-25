package com.yingqi.flash.access;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yingqi.flash.domain.FlashUser;
import com.yingqi.flash.redis.FlashKey;
import com.yingqi.flash.redis.RedisService;
import com.yingqi.flash.result.CodeMsg;
import com.yingqi.flash.result.Result;
import com.yingqi.flash.service.FlashUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    FlashUserService flashUserService;
    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            //找到特定annotation
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit annotation = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if (annotation == null) {
                return true;
            }
            //利用线程安全的ThreadLocal保存用户
            FlashUser user = getUser(request, response);
            UserContext.setUserHolder(user);
            //获取参数
            int maxCount = annotation.maxCount();
            boolean needLogin = annotation.needLogin();
            int seconds = annotation.seconds();
            String key = request.getRequestURI() + "_" + getIpAddress(request);
            //检查用户是否登陆
            if (needLogin) {
                if (user == null) {
                    reder(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            }
            //查询访问次数
            FlashKey accesscount = FlashKey.ACCESSCOUNT(seconds);
            Integer count = redisService.get(accesscount, key, Integer.class);
            if (count == null) {
                redisService.set(accesscount, key, 1);
            } else if (count < maxCount) {
                redisService.incr(accesscount, key);
            } else {
                reder(response, CodeMsg.ACCESS_ILLEGAL);
                return false;
            }
        }
        return true;
    }

    //响应信息
    private void reder(HttpServletResponse response, CodeMsg error) throws IOException {
        response.setContentType("application/josn;charset=UTF-8");
        String key = JSON.toJSONString(Result.error(error));
        ServletOutputStream out = response.getOutputStream();
        out.write(key.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    //获取用户
    private FlashUser getUser(HttpServletRequest request, HttpServletResponse response) {

        String paramToken = request.getParameter(FlashUserService.COOKIE_NAME_TOKEN);

        String cookieToken = getCoookieValue(request, FlashUserService.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(paramToken) && StringUtils.isEmpty(cookieToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return flashUserService.getByToken(response, token);
    }

    private String getCoookieValue(HttpServletRequest request, String cookieNameToken) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieNameToken)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 获取Ip地址
     *
     * @param request
     * @return
     */
    private String getIpAddress(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if (index != -1) {
                return XFor.substring(0, index);
            } else {
                return XFor;
            }
        }
        XFor = Xip;
        if (StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            return XFor;
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
    }
}
