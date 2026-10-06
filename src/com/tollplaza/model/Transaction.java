package com.tollplaza.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Transaction class stores details of a toll collection event.
 * Uses Wrapper Classes (Double, Integer) for fee calculation and totals.
 */
public class Transaction implements Comparable<Transaction> {
    private String transactionId;
    private String vehicleNumber;
    private VehicleCategory vehicleCategory;
    private Integer laneNumber;        // Wrapper class
    private Double tollAmount;         // Wrapper class
    private String paymentMode;        // CASH or TAG
    private Date transactionTime;
    private String status;

    // Constructor for creating a new transaction
    public Transaction(String transactionId, String vehicleNumber, VehicleCategory vehicleCategory,
                       Integer laneNumber, Double tollAmount, String paymentMode) {
        this.transactionId = transactionId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleCategory = vehicleCategory;
        this.laneNumber = laneNumber;
        this.tollAmount = tollAmount;
        this.paymentMode = paymentMode;
        this.transactionTime = new Date();
        this.status = "SUCCESS";
    }

    // Constructor used when loading saved transactions from file
    public Transaction(String transactionId, String vehicleNumber, VehicleCategory vehicleCategory,
                       Integer laneNumber, Double tollAmount, String paymentMode, Date transactionTime, String status) {
        this.transactionId = transactionId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleCategory = vehicleCategory;
        this.laneNumber = laneNumber;
        this.tollAmount = tollAmount;
        this.paymentMode = paymentMode;
        this.transactionTime = transactionTime;
        this.status = status;
    }

    // Comparable implementation for natural sorting by toll amount
    @Override
    public int compareTo(Transaction other) {
        if (other == null) {
            return 1;
        }
        return this.tollAmount.compareTo(other.tollAmount);
    }

    // Format transaction timestamp for display
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(transactionTime);
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public VehicleCategory getVehicleCategory() {
        return vehicleCategory;
    }

    public void setVehicleCategory(VehicleCategory vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }

    public Integer getLaneNumber() {
        return laneNumber;
    }

    public void setLaneNumber(Integer laneNumber) {
        this.laneNumber = laneNumber;
    }

    public Double getTollAmount() {
        return tollAmount;
    }

    public void setTollAmount(Double tollAmount) {
        this.tollAmount = tollAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Transaction [ID=" + transactionId + ", Vehicle=" + vehicleNumber + ", Amount=Rs. " + tollAmount + "]";
    }
}
