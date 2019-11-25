package com.yingqi.flash.dao;

import com.yingqi.flash.domain.FlashUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FlashUserDao {
    @Select("select * from flash_user where id=#{id}")
    public FlashUser getById(Long id);
    @Update("update flash_user set password = #{password} where id =#{id}")
    void update(FlashUser flashUser);
}
