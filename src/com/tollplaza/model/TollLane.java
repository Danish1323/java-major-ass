package com.tollplaza.model;

import java.util.LinkedList;

/**
 * TollLane represents an individual lane at the toll plaza.
 * Maintains lane-wise transaction history using LinkedList.
 */
public class TollLane {
    private int laneId;
    private String laneName;
    private boolean isTagOnly; // true if dedicated FastTag / Electronic tag lane
    private LinkedList<Transaction> laneTransactions; // Requirement: LinkedList to maintain lane history
    private double totalCollection;

    // Parameterized constructor
    public TollLane(int laneId, String laneName, boolean isTagOnly) {
        this.laneId = laneId;
        this.laneName = laneName;
        this.isTagOnly = isTagOnly;
        this.laneTransactions = new LinkedList<Transaction>();
        this.totalCollection = 0.0;
    }

    // Add a completed transaction to this lane's history
    public void addTransaction(Transaction tx) {
        this.laneTransactions.add(tx);
        if ("SUCCESS".equalsIgnoreCase(tx.getStatus())) {
            this.totalCollection = this.totalCollection + tx.getTollAmount();
        }
    }

    // Get count of transactions processed on this lane
    public int getTransactionCount() {
        return this.laneTransactions.size();
    }

    // Getters and Setters
    public int getLaneId() {
        return laneId;
    }

    public void setLaneId(int laneId) {
        this.laneId = laneId;
    }

    public String getLaneName() {
        return laneName;
    }

    public void setLaneName(String laneName) {
        this.laneName = laneName;
    }

    public boolean isTagOnly() {
        return isTagOnly;
    }

    public void setTagOnly(boolean isTagOnly) {
        this.isTagOnly = isTagOnly;
    }

    public LinkedList<Transaction> getLaneTransactions() {
        return laneTransactions;
    }

    public double getTotalCollection() {
        return totalCollection;
    }

    @Override
    public String toString() {
        return "TollLane #" + laneId + " (" + laneName + ")" + (isTagOnly ? " [FASTag Lane]" : " [Hybrid Lane]")
                + " - Total: Rs. " + totalCollection;
    }
}
