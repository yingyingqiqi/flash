package com.yingqi.flash.dao;

import com.yingqi.flash.domain.FlashGoods;
import com.yingqi.flash.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
@Mapper
public interface GoodsDao {

    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.flash_price from flash_goods mg left join goods g on mg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.flash_price from flash_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsid}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsid") long goodsid);
    @Update("update flash_goods set stock_count = stock_count -1 where goods_id = #{goodsId} and stock_count > 0")
    int reduceStock(FlashGoods g);

}
