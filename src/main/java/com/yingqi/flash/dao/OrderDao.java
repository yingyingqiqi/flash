package com.yingqi.flash.dao;

import com.yingqi.flash.domain.FlashOrder;
import com.yingqi.flash.domain.OrderInfo;
import com.yingqi.flash.vo.GoodsVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderDao {
    @Select("select * from flash_order where user_id=#{userId} and goods_id=#{goodsId}")
    FlashOrder getFlashOrderByUserIdGoodsId(@Param("userId") Long userId, @Param("goodsId") long goodsId);
    @Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)values("
            + "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = long.class,before = false,statement = "select last_insert_id()")
    long insert(OrderInfo orderInfo);

    @Insert("insert into flash_order (user_id, goods_id, order_id)values(#{userId}, #{goodsId}, #{orderId})")
    void insertFlashOrder(FlashOrder flashOrder);

    @Select("select * from order_info where id=#{orderId}")
    OrderInfo getOrderById(long orderId);
}
