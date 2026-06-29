package com.teatrack_mcd_253eie502802_group02.model;

import java.io.Serializable;
import java.util.List;

public class FirebaseOrder implements Serializable {

    private String id;
    private String orderId;
    private String title;
    private String meta;
    private String date;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String branchAddress;
    private String paymentMethod;
    private String status;
    private int subtotal;
    private int shipping;
    private int discount;
    private int total;
    private List<FirebaseOrderItem> items;
    private String deliveryDate;
    private String deliveryTime;
    private String note;
    private String couponCode;
    private String userId;
    private String createdAt;
    private String updatedAt;
    private String agencyId;

    public FirebaseOrder() {
    }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMeta() { return meta; }
    public void setMeta(String meta) { this.meta = meta; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getBranchAddress() { return branchAddress; }
    public void setBranchAddress(String branchAddress) { this.branchAddress = branchAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }

    public int getShipping() { return shipping; }
    public void setShipping(int shipping) { this.shipping = shipping; }

    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public List<FirebaseOrderItem> getItems() { return items; }
    public void setItems(List<FirebaseOrderItem> items) { this.items = items; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
