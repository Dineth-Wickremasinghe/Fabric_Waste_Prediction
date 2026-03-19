package com.dashboard.cuttingrisk.model;

public class WasteRequest {
    private double fabricWidth;
    private int layers;
    private int orderQty;
    private String fabricType;
    private String style;

    public double getFabricWidth() { return fabricWidth; }
    public void setFabricWidth(double fabricWidth) { this.fabricWidth = fabricWidth; }

    public int getLayers() { return layers; }
    public void setLayers(int layers) { this.layers = layers; }

    public int getOrderQty() { return orderQty; }
    public void setOrderQty(int orderQty) { this.orderQty = orderQty; }

    public String getFabricType() { return fabricType; }
    public void setFabricType(String fabricType) { this.fabricType = fabricType; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
}
