package com.yingqi.flash.validator;

import com.yingqi.flash.util.ValidatorUtil;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

public class IsMobileValidator implements ConstraintValidator<IsModile,String> {

    private boolean required = false;

    @Override
    public void initialize(IsModile isModile) {
        required = isModile.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (required) {
            return ValidatorUtil.isMobile(s);
        }else {
            if (StringUtils.isEmpty(s)) {
                return true;
            }else{
                return ValidatorUtil.isMobile(s);
            }
        }
    }
}
