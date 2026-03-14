package com.sagefolio.model;

import java.util.List;

public class StockAnalysis {
    private String symbol;
    private double currentPrice;
    private double yearReturn;
    private String trend;
    private double stddev;
    private double atr;
    private double drawdown;
    private List<String> dates;
    private List<Double> closePrices;
    private List<Double> ma20;
    private String volatilityLabel;

    public StockAnalysis() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getYearReturn() { return yearReturn; }
    public void setYearReturn(double yearReturn) { this.yearReturn = yearReturn; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public double getStddev() { return stddev; }
    public void setStddev(double stddev) { this.stddev = stddev; }

    public double getAtr() { return atr; }
    public void setAtr(double atr) { this.atr = atr; }

    public double getDrawdown() { return drawdown; }
    public void setDrawdown(double drawdown) { this.drawdown = drawdown; }

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }

    public List<Double> getClosePrices() { return closePrices; }
    public void setClosePrices(List<Double> closePrices) { this.closePrices = closePrices; }

    public List<Double> getMa20() { return ma20; }
    public void setMa20(List<Double> ma20) { this.ma20 = ma20; }

    public String getVolatilityLabel() { return volatilityLabel; }
    public void setVolatilityLabel(String volatilityLabel) { this.volatilityLabel = volatilityLabel; }
}
