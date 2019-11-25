package com.yingqi.flash.vo;

import com.yingqi.flash.validator.IsModile;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class LoginVo {
    @NotNull(message = "手机号码为空")
    @IsModile
    private String mobile;

//    @NotNull(message = "手机号码不为空")
//    @Pattern(regexp = "1\\d{10}", message = "手机号码格式错误")
//    private String mobile2;

    @NotNull
    @Length(min = 32)
    private String password;

    @Override
    public String toString() {
        return "LoginVo{" +
                "mobile='" + mobile + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String moblie) {
        this.mobile = moblie;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
