package com.sagefolio.model;

public class Centroid {
    private int cluster;
    private String volatilityLabel;
    private double stddevN;
    private double atrN;
    private double drawdownN;

    public Centroid() {}

    public int getCluster() { return cluster; }
    public void setCluster(int cluster) { this.cluster = cluster; }

    public String getVolatilityLabel() { return volatilityLabel; }
    public void setVolatilityLabel(String volatilityLabel) { this.volatilityLabel = volatilityLabel; }

    public double getStddevN() { return stddevN; }
    public void setStddevN(double stddevN) { this.stddevN = stddevN; }

    public double getAtrN() { return atrN; }
    public void setAtrN(double atrN) { this.atrN = atrN; }

    public double getDrawdownN() { return drawdownN; }
    public void setDrawdownN(double drawdownN) { this.drawdownN = drawdownN; }
}
