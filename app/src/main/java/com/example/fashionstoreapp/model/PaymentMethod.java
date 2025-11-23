package com.example.fashionstoreapp.model;

public class PaymentMethod {
    private String paymentId;
    private String type;
    private boolean isDefault;

    public PaymentMethod() {
    }

    public PaymentMethod(String paymentId, String type, boolean isDefault) {
        this.paymentId = paymentId;
        this.type = type;
        this.isDefault = isDefault;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getType() {
        return type;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
