package com.xuangand.rhythmoflife.model;

import com.google.gson.Gson;

import java.time.LocalDateTime;

public class PurchasedHistory {
    private String id;
    private String user_id;
    private String transaction_token;
    private Double amount;
    private LocalDateTime date;

    public PurchasedHistory() {
    }

    public PurchasedHistory(String id, String user_id, String transaction_token, Double amount, LocalDateTime date) {
        this.id = id;
        this.user_id = user_id;
        this.transaction_token = transaction_token;
        this.amount = amount;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTransaction_token() {
        return transaction_token;
    }

    public void setTransaction_token(String transaction_token) {
        this.transaction_token = transaction_token;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public String toJSon() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
