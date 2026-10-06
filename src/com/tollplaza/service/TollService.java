package com.tollplaza.service;

import com.tollplaza.exception.InvalidPaymentException;
import com.tollplaza.exception.InvalidVehicleException;
import com.tollplaza.model.TollLane;
import com.tollplaza.model.TollTag;
import com.tollplaza.model.Transaction;
import com.tollplaza.model.VehicleCategory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * TollService contains all the business logic for toll collection,
 * vehicle search, sorting, and reporting.
 */
public class TollService {

    // Collections required by problem statement
    private ArrayList<Transaction> transactionList;
    private HashMap<String, ArrayList<Transaction>> vehicleTransactionMap;
    private TreeMap<Double, ArrayList<Transaction>> amountSortedMap;

    // Toll plaza configuration
    private ArrayList<TollLane> lanes;
    private HashMap<String, TollTag> registeredTags;

    // Transaction ID counter
    private int counter = 1000;

    // File name for storing data
    private static final String FILE_NAME = "toll_transactions.txt";

    // Constructor to set up data structures and sample data
    public TollService() {
        transactionList = new ArrayList<Transaction>();
        vehicleTransactionMap = new HashMap<String, ArrayList<Transaction>>();
        amountSortedMap = new TreeMap<Double, ArrayList<Transaction>>();
        lanes = new ArrayList<TollLane>();
        registeredTags = new HashMap<String, TollTag>();

        // Set up standard 4 toll lanes
        lanes.add(new TollLane(1, "North Express Lane", true));
        lanes.add(new TollLane(2, "North General Lane", false));
        lanes.add(new TollLane(3, "South Express Lane", true));
        lanes.add(new TollLane(4, "South General Lane", false));

        // Sample FASTags for testing
        registerTag(new TollTag("TAG101", "MH12AB1234", 1000.0));
        registerTag(new TollTag("TAG102", "DL01XY9876", 500.0));
        registerTag(new TollTag("TAG103", "KA05MN4321", 80.0));
        registerTag(new TollTag("TAG104", "HR26CD5566", 1200.0));

        // Load existing transactions from text file if available
        loadTransactionsFromFile();
    }

    // Register a new FASTag in the system
    public void registerTag(TollTag tag) {
        if (tag != null && tag.getTagId() != null) {
            registeredTags.put(tag.getTagId().toUpperCase(), tag);
        }
    }

    // Get tag by ID
    public TollTag getTagById(String tagId) {
        if (tagId == null) return null;
        return registeredTags.get(tagId.trim().toUpperCase());
    }

    // Get tag by vehicle number
    public TollTag getTagByVehicleNumber(String vehicleNumber) {
        if (vehicleNumber == null) return null;
        for (TollTag tag : registeredTags.values()) {
            if (tag.getVehicleNumber().equalsIgnoreCase(vehicleNumber.trim())) {
                return tag;
            }
        }
        return null;
    }

    // Calculate toll fee for given category
    public Double calculateFee(VehicleCategory category) {
        if (category == null) {
            return 100.0;
        }
        return category.getBaseRate();
    }

    // Process toll collection for a vehicle passing a lane
    public Transaction processToll(String vehicleNumber, VehicleCategory category,
                                   int laneNumber, String paymentMode, String tagId)
            throws InvalidVehicleException, InvalidPaymentException {

        // Validate vehicle number and payment mode
        Validator.validateVehicleNumber(vehicleNumber);
        Validator.validatePaymentMode(paymentMode);

        String vNum = vehicleNumber.trim().toUpperCase();
        String mode = paymentMode.trim().toUpperCase();

        // Get lane
        TollLane lane = getLaneById(laneNumber);
        if (lane == null) {
            throw new IllegalArgumentException("Invalid lane: " + laneNumber);
        }

        // Lane constraint check
        if (lane.isTagOnly() && !mode.equals("TAG")) {
            throw new InvalidPaymentException("Lane " + laneNumber + " is FASTag only! Cash not allowed.");
        }

        // Calculate toll amount
        Double fee = calculateFee(category);
        Validator.validateFeeAmount(fee);

        // Deduct from FASTag if paying by tag
        if (mode.equals("TAG")) {
            TollTag tag = null;
            if (tagId != null && !tagId.trim().isEmpty()) {
                tag = getTagById(tagId);
            } else {
                tag = getTagByVehicleNumber(vNum);
            }

            if (tag == null) {
                throw new InvalidPaymentException("No FASTag registered for vehicle: " + vNum);
            }

            // Deduct balance from tag
            tag.deductAmount(fee);
        }

        // Create transaction
        counter++;
        String txId = "TXN" + counter;
        Transaction tx = new Transaction(txId, vNum, category, laneNumber, fee, mode);

        // Store in ArrayList
        transactionList.add(tx);

        // Store in HashMap (Vehicle Number -> Transactions)
        if (!vehicleTransactionMap.containsKey(vNum)) {
            vehicleTransactionMap.put(vNum, new ArrayList<Transaction>());
        }
        vehicleTransactionMap.get(vNum).add(tx);

        // Store in TreeMap (Amount -> Transactions)
        if (!amountSortedMap.containsKey(fee)) {
            amountSortedMap.put(fee, new ArrayList<Transaction>());
        }
        amountSortedMap.get(fee).add(tx);

        // Add to lane history (LinkedList)
        lane.addTransaction(tx);

        // Persist to file
        saveTransactionsToFile();

        return tx;
    }

    // ================= CRUD OPERATIONS =================

    // READ: Get all transactions
    public ArrayList<Transaction> getAllTransactions() {
        return transactionList;
    }

    // SEARCH: Search transactions by vehicle number using HashMap
    public ArrayList<Transaction> searchByVehicleNumber(String vehicleNumber) {
        if (vehicleNumber == null) return new ArrayList<Transaction>();
        String vNum = vehicleNumber.trim().toUpperCase();
        if (vehicleTransactionMap.containsKey(vNum)) {
            return vehicleTransactionMap.get(vNum);
        }
        return new ArrayList<Transaction>();
    }

    // SEARCH: Search transactions by lane number
    public ArrayList<Transaction> searchByLane(int laneNumber) {
        ArrayList<Transaction> results = new ArrayList<Transaction>();
        for (Transaction tx : transactionList) {
            if (tx.getLaneNumber() == laneNumber) {
                results.add(tx);
            }
        }
        return results;
    }

    // Find a single transaction by ID
    public Transaction findTransactionById(String txId) {
        for (Transaction tx : transactionList) {
            if (tx.getTransactionId().equalsIgnoreCase(txId)) {
                return tx;
            }
        }
        return null;
    }

    // UPDATE: Update a transaction's lane and payment mode
    public boolean updateTransaction(String txId, int newLane, String newPaymentMode) throws InvalidPaymentException {
        Validator.validatePaymentMode(newPaymentMode);
        Transaction tx = findTransactionById(txId);
        if (tx == null) {
            return false;
        }

        tx.setLaneNumber(newLane);
        tx.setPaymentMode(newPaymentMode.toUpperCase());
        saveTransactionsToFile();
        return true;
    }

    // DELETE: Delete a transaction from all collections
    public boolean deleteTransaction(String txId) {
        Transaction tx = findTransactionById(txId);
        if (tx == null) {
            return false;
        }

        // Remove from list
        transactionList.remove(tx);

        // Remove from HashMap
        String vNum = tx.getVehicleNumber();
        if (vehicleTransactionMap.containsKey(vNum)) {
            vehicleTransactionMap.get(vNum).remove(tx);
        }

        // Remove from TreeMap
        Double amt = tx.getTollAmount();
        if (amountSortedMap.containsKey(amt)) {
            amountSortedMap.get(amt).remove(tx);
        }

        saveTransactionsToFile();
        return true;
    }

    // ================= SORTING OPERATIONS =================

    // Sort by amount ascending using TreeMap
    public ArrayList<Transaction> getTransactionsSortedByAmountAscending() {
        ArrayList<Transaction> sorted = new ArrayList<Transaction>();
        for (Double amt : amountSortedMap.keySet()) {
            sorted.addAll(amountSortedMap.get(amt));
        }
        return sorted;
    }

    // Sort by amount descending using Comparator
    public ArrayList<Transaction> getTransactionsSortedByAmountDescending() {
        ArrayList<Transaction> sorted = new ArrayList<Transaction>(transactionList);
        Collections.sort(sorted, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                return t2.getTollAmount().compareTo(t1.getTollAmount());
            }
        });
        return sorted;
    }

    // Sort by time using Comparator
    public ArrayList<Transaction> getTransactionsSortedByTime(final boolean recentFirst) {
        ArrayList<Transaction> sorted = new ArrayList<Transaction>(transactionList);
        Collections.sort(sorted, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                if (recentFirst) {
                    return t2.getTransactionTime().compareTo(t1.getTransactionTime());
                } else {
                    return t1.getTransactionTime().compareTo(t2.getTransactionTime());
                }
            }
        });
        return sorted;
    }

    // ================= SUMMARY & REPORTS =================

    // Calculate total money collected
    public double getTotalPlazaCollection() {
        double total = 0.0;
        for (Transaction tx : transactionList) {
            total = total + tx.getTollAmount();
        }
        return total;
    }

    // Get collection per lane
    public Map<Integer, Double> getLaneWiseCollection() {
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        for (TollLane lane : lanes) {
            map.put(lane.getLaneId(), lane.getTotalCollection());
        }
        return map;
    }

    // Get collection per vehicle category
    public Map<VehicleCategory, Double> getCategoryWiseCollection() {
        Map<VehicleCategory, Double> map = new HashMap<VehicleCategory, Double>();
        for (VehicleCategory cat : VehicleCategory.values()) {
            map.put(cat, 0.0);
        }
        for (Transaction tx : transactionList) {
            VehicleCategory cat = tx.getVehicleCategory();
            double old = map.get(cat);
            map.put(cat, old + tx.getTollAmount());
        }
        return map;
    }

    // Generate daily audit report as text
    public String generateTollReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("          HIGHWAY TOLL PLAZA - DAILY AUDIT REPORT           \n");
        sb.append("============================================================\n");
        sb.append("Generated On: ").append(new Date().toString()).append("\n\n");
        sb.append("Total Vehicles Processed : ").append(transactionList.size()).append("\n");
        sb.append("Total Revenue Collected  : Rs. ").append(getTotalPlazaCollection()).append("\n\n");

        sb.append("--- LANE-WISE COLLECTIONS ---\n");
        for (TollLane lane : lanes) {
            sb.append("Lane ").append(lane.getLaneId()).append(" (")
                    .append(lane.getLaneName()).append("): Rs. ")
                    .append(lane.getTotalCollection())
                    .append(" [").append(lane.getTransactionCount()).append(" vehicles]\n");
        }

        sb.append("\n--- VEHICLE CATEGORY BREAKDOWN ---\n");
        Map<VehicleCategory, Double> catMap = getCategoryWiseCollection();
        for (VehicleCategory cat : VehicleCategory.values()) {
            sb.append(cat.name()).append(" (Base Rate Rs. ")
                    .append(cat.getBaseRate()).append("): Rs. ")
                    .append(catMap.get(cat)).append("\n");
        }

        sb.append("============================================================\n");
        return sb.toString();
    }

    // ================= FILE PERSISTENCE =================

    // Save transactions to toll_transactions.txt
    public void saveTransactionsToFile() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_NAME));
            for (Transaction tx : transactionList) {
                String line = tx.getTransactionId() + ","
                        + tx.getVehicleNumber() + ","
                        + tx.getVehicleCategory().name() + ","
                        + tx.getLaneNumber() + ","
                        + tx.getTollAmount() + ","
                        + tx.getPaymentMode() + ","
                        + tx.getTransactionTime().getTime() + ","
                        + tx.getStatus();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save to file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // Load transactions from toll_transactions.txt
    public void loadTransactionsFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    String txId = parts[0].trim();
                    String vNum = parts[1].trim();
                    VehicleCategory cat = VehicleCategory.valueOf(parts[2].trim());
                    int lane = Integer.parseInt(parts[3].trim());
                    double amt = Double.parseDouble(parts[4].trim());
                    String mode = parts[5].trim();
                    long timeMs = Long.parseLong(parts[6].trim());
                    String status = parts[7].trim();

                    Transaction tx = new Transaction(txId, vNum, cat, lane, amt, mode, new Date(timeMs), status);

                    // Add to list and maps
                    transactionList.add(tx);

                    if (!vehicleTransactionMap.containsKey(vNum)) {
                        vehicleTransactionMap.put(vNum, new ArrayList<Transaction>());
                    }
                    vehicleTransactionMap.get(vNum).add(tx);

                    if (!amountSortedMap.containsKey(amt)) {
                        amountSortedMap.put(amt, new ArrayList<Transaction>());
                    }
                    amountSortedMap.get(amt).add(tx);

                    TollLane l = getLaneById(lane);
                    if (l != null) {
                        l.addTransaction(tx);
                    }

                    // Keep counter updated
                    if (txId.startsWith("TXN")) {
                        try {
                            int num = Integer.parseInt(txId.substring(3));
                            if (num > counter) {
                                counter = num;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load from file: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // Getters for GUI
    public ArrayList<TollLane> getLanes() {
        return lanes;
    }

    public TollLane getLaneById(int laneId) {
        for (TollLane lane : lanes) {
            if (lane.getLaneId() == laneId) {
                return lane;
            }
        }
        return null;
    }

    public HashMap<String, TollTag> getRegisteredTags() {
        return registeredTags;
    }
}
