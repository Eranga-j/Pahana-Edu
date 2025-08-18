package com.pahanaedu.service.model;
import java.math.BigDecimal;
public class Item {
    private int id; private String sku; private String name; private BigDecimal unitPrice;
    public Item() {}
    public Item(int id,String sku,String name,BigDecimal unitPrice){this.id=id;this.sku=sku;this.name=name;this.unitPrice=unitPrice;}
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getSku(){return sku;} public void setSku(String v){this.sku=v;}
    public String getName(){return name;} public void setName(String v){this.name=v;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){this.unitPrice=v;}
}
