package com.yundian.star.been;

/**
 * Created by Administrator on 2017/6/16.
 */

public class OrderSucReturnBeen {

    /**
     * orderId : 4596320529293132300
     * buyUid : 142
     * sellUid : 152
     * amount : 22
     * openPositionTime : 1497340525
     * openPrice : 22.1
     * symbol : 1001
     */

    private long orderId;
    private int buyUid;
    private int sellUid;
    private int amount;
    private int openPositionTime;
    private double openPrice;
    private String symbol;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public int getBuyUid() {
        return buyUid;
    }

    public void setBuyUid(int buyUid) {
        this.buyUid = buyUid;
    }

    public int getSellUid() {
        return sellUid;
    }

    public void setSellUid(int sellUid) {
        this.sellUid = sellUid;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getOpenPositionTime() {
        return openPositionTime;
    }

    public void setOpenPositionTime(int openPositionTime) {
        this.openPositionTime = openPositionTime;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
