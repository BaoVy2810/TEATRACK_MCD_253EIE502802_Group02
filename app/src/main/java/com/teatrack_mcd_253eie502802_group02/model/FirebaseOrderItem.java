package com.teatrack_mcd_253eie502802_group02.model;

public class FirebaseOrderItem implements java.io.Serializable {

    private String productId;
    private String productName;
    private String image;
    private int quantity;
    private int unitPrice;
    private int lineTotal;
    private String size;
    private String sugar;
    private String ice;
    private String toppings;

    public FirebaseOrderItem() {
    }

    public FirebaseOrderItem(String productId, String productName, String image, int quantity,
                             int unitPrice, int lineTotal, String size, String sugar, String ice,
                             String toppings) {
        this.productId = productId;
        this.productName = productName;
        this.image = image;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.size = size;
        this.sugar = sugar;
        this.ice = ice;
        this.toppings = toppings;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }

    public int getLineTotal() { return lineTotal; }
    public void setLineTotal(int lineTotal) { this.lineTotal = lineTotal; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getSugar() { return sugar; }
    public void setSugar(String sugar) { this.sugar = sugar; }

    public String getIce() { return ice; }
    public void setIce(String ice) { this.ice = ice; }

    public String getToppings() { return toppings; }
    public void setToppings(String toppings) { this.toppings = toppings; }
}
