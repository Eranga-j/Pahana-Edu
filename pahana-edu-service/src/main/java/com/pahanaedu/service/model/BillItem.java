package com.pahanaedu.service.model;
import java.math.BigDecimal;
public class BillItem {
    private int id; private int itemId; private String itemName; private int qty; private BigDecimal unitPrice; private BigDecimal lineTotal;
    public BillItem(){}
    public BillItem(int id,int itemId,String itemName,int qty,BigDecimal unitPrice,BigDecimal lineTotal){
        this.id=id;this.itemId=itemId;this.itemName=itemName;this.qty=qty;this.unitPrice=unitPrice;this.lineTotal=lineTotal;}
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public int getItemId(){return itemId;} public void setItemId(int v){this.itemId=v;}
    public String getItemName(){return itemName;} public void setItemName(String v){this.itemName=v;}
    public int getQty(){return qty;} public void setQty(int v){this.qty=v;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){this.unitPrice=v;}
    public BigDecimal getLineTotal(){return lineTotal;} public void setLineTotal(BigDecimal v){this.lineTotal=v;}
}
