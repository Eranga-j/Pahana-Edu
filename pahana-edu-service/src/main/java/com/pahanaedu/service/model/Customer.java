package com.pahanaedu.service.model;
public class Customer {
    private int id; private String accountNumber; private String name; private String address; private String phone;
    public Customer() {}
    public Customer(int id,String accountNumber,String name,String address,String phone){
        this.id=id;this.accountNumber=accountNumber;this.name=name;this.address=address;this.phone=phone;}
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getAccountNumber(){return accountNumber;} public void setAccountNumber(String v){this.accountNumber=v;}
    public String getName(){return name;} public void setName(String v){this.name=v;}
    public String getAddress(){return address;} public void setAddress(String v){this.address=v;}
    public String getPhone(){return phone;} public void setPhone(String v){this.phone=v;}
}
