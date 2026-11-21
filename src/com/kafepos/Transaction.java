
package com.kafepos;

public class Transaction {

    private String transactionId;
    private String dateTime;
    private String customer;
    private String paymentMethod;
    private String amount;
    private String status;

    public Transaction(String transactionId, String dateTime,
                       String customer, String paymentMethod,
                       String amount, String status) {
        this.transactionId = transactionId;
        this.dateTime = dateTime;
        this.customer = customer;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getCustomer() {
        return customer;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}