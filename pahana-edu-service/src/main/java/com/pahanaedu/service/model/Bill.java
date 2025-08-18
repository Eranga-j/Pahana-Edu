package com.pahanaedu.service.model;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.List;
public class Bill {
    private int id; private String billNo; private int customerId; private String customerName;
    private BigDecimal totalAmount; private LocalDateTime createdAt; private int createdBy; private List<BillItem> items;
    public Bill(){}
    public Bill(int id,String billNo,int customerId,String customerName,BigDecimal totalAmount,LocalDateTime createdAt,int createdBy,List<BillItem> items){
        this.id=id;this.billNo=billNo;this.customerId=customerId;this.customerName=customerName;this.totalAmount=totalAmount;this.createdAt=createdAt;this.createdBy=createdBy;this.items=items;}
    public int getId(){return id;} public void setId(int v){this.id=v;}
    public String getBillNo(){return billNo;} public void setBillNo(String v){this.billNo=v;}
    public int getCustomerId(){return customerId;} public void setCustomerId(int v){this.customerId=v;}
    public String getCustomerName(){return customerName;} public void setCustomerName(String v){this.customerName=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){this.totalAmount=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public int getCreatedBy(){return createdBy;} public void setCreatedBy(int v){this.createdBy=v;}
    public java.util.List<BillItem> getItems(){return items;} public void setItems(java.util.List<BillItem> v){this.items=v;}
}
