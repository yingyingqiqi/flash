package com.yingqi.flash.vo;

import com.yingqi.flash.domain.Goods;

import java.util.Date;

public class GoodsVo extends Goods {
    private Double flashPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;

    public Double getFlashPrice() {
        return flashPrice;
    }

    public void setFlshaPrice(Double flashaPrice) {
        this.flashPrice = flashaPrice;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "GoodsVo{" +super.toString()+
                "flashPrice=" + flashPrice +
                ", stockCount=" + stockCount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
