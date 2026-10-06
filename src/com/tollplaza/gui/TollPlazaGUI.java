package com.tollplaza.gui;

import com.tollplaza.exception.InvalidPaymentException;
import com.tollplaza.exception.InvalidVehicleException;
import com.tollplaza.model.TollLane;
import com.tollplaza.model.TollTag;
import com.tollplaza.model.Transaction;
import com.tollplaza.model.VehicleCategory;
import com.tollplaza.service.TollService;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TollPlazaGUI: Complete Swing-based GUI for the Toll Plaza Fee Collection & Management System.
 * Demonstrates:
 * - JFrame, JPanel, JSplitPane, JTabbedPane
 * - Layout Managers: BorderLayout, GridLayout, FlowLayout, GridBagLayout
 * - Components: JLabel, JTextField, JComboBox, JRadioButton, ButtonGroup, JButton, JTable, JTextArea
 * - Menus: JMenuBar, JMenu, JMenuItem
 * - Dialog Boxes: JOptionPane (Information, Warning, Error, Confirm)
 * - Event Handling: ActionListener, ItemListener
 * - Multithreading: SwingUtilities.invokeLater
 */
public class TollPlazaGUI extends JFrame {

    // Backend service reference
    private TollService tollService;

    // GUI Components for Toll Entry
    private JTextField txtVehicleNumber;
    private JComboBox<VehicleCategory> comboCategory;
    private JComboBox<String> comboLane;
    private JRadioButton radioCash;
    private JRadioButton radioTag;
    private ButtonGroup paymentGroup;
    private JTextField txtTagId;
    private JLabel lblCalculatedFee;
    private JButton btnCollectToll;
    private JButton btnClearForm;

    // Search & Filter components
    private JTextField txtSearchVehicle;
    private JComboBox<String> comboFilterLane;
    private JButton btnSearchVehicle;
    private JButton btnFilterLane;
    private JButton btnResetSearch;

    // Sort buttons
    private JButton btnSortAmountAsc;
    private JButton btnSortAmountDesc;
    private JButton btnSortTime;

    // Action buttons for CRUD
    private JButton btnDeleteTx;

    // JTable for displaying transactions
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    // Summary Labels
    private JLabel lblTotalCollection;
    private JLabel lblTotalVehicles;
    private JTextArea txtLaneSummary;
    private JTextArea txtCategorySummary;

    // Constructor
    public TollPlazaGUI(TollService tollService) {
        super("Highway Toll Plaza Fee Collection & Management System");
        this.tollService = tollService;

        // Set up Frame properties
        setSize(1100, 750);
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Build Menu Bar
        setupMenuBar();

        // Main Layout container
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Top Header Banner
        JPanel headerPanel = createHeaderPanel();
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Split Pane: Left (Collection Form) & Right (Table & Tabs)
        JPanel leftPanel = createEntryFormPanel();
        JPanel rightPanel = createDisplayPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(360);
        splitPane.setContinuousLayout(true);
        mainContainer.add(splitPane, BorderLayout.CENTER);

        // 3. Bottom Status / Summary Banner
        JPanel bottomPanel = createBottomSummaryPanel();
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);

        // Add main container to Frame
        add(mainContainer);

        // Initial Data Load
        refreshTableData(tollService.getAllTransactions());
        updateSummaryBanner();
    }

    // Menu Bar Setup (JMenuBar, JMenu, JMenuItem)
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu menuFile = new JMenu("File");
        JMenuItem itemSave = new JMenuItem("Save Data");
        JMenuItem itemGenerateReport = new JMenuItem("Generate Audit Report");
        JMenuItem itemExit = new JMenuItem("Exit");

        itemSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tollService.saveTransactionsToFile();
                JOptionPane.showMessageDialog(TollPlazaGUI.this,
                        "All transactions saved successfully to 'toll_transactions.txt'!",
                        "Data Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        itemGenerateReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAuditReportDialog();
            }
        });

        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(TollPlazaGUI.this,
                        "Are you sure you want to exit the application?",
                        "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    tollService.saveTransactionsToFile();
                    System.exit(0);
                }
            }
        });

        menuFile.add(itemSave);
        menuFile.add(itemGenerateReport);
        menuFile.addSeparator();
        menuFile.add(itemExit);

        // FastTag Menu
        JMenu menuTags = new JMenu("Electronic Tags");
        JMenuItem itemViewTags = new JMenuItem("View Registered Tags");
        JMenuItem itemRechargeTag = new JMenuItem("Recharge Tag");

        itemViewTags.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisteredTagsDialog();
            }
        });

        itemRechargeTag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRechargeTagDialog();
            }
        });

        menuTags.add(itemViewTags);
        menuTags.add(itemRechargeTag);

        // Help Menu
        JMenu menuHelp = new JMenu("Help");
        JMenuItem itemAbout = new JMenuItem("About System");
        itemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(TollPlazaGUI.this,
                        "Highway Toll Plaza Fee Collection & Management System\n"
                                + "Developed for Java Programming Exam Project\n"
                                + "Features: OOP, Enum, Collections, File I/O, Swing GUI, Custom Exceptions",
                        "About Project", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menuHelp.add(itemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuTags);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);
    }

    // Top Header Banner
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185)); // Elegant blue
        panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel title = new JLabel("HIGHWAY TOLL PLAZA MANAGEMENT SYSTEM");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subTitle = new JLabel("Electronic & Cash Fee Collection Center");
        subTitle.setFont(new Font("Arial", Font.ITALIC, 12));
        subTitle.setForeground(new Color(236, 240, 241));

        panel.add(title, BorderLayout.WEST);
        panel.add(subTitle, BorderLayout.EAST);
        return panel;
    }

    // Left Panel: Toll Entry Form
    private JPanel createEntryFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                " Vehicle Toll Entry Form ",
                0, 0, new Font("Arial", Font.BOLD, 14), new Color(41, 128, 185)));

        JPanel formGrid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Vehicle Number
        gbc.gridx = 0; gbc.gridy = 0;
        formGrid.add(new JLabel("Vehicle Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        txtVehicleNumber = new JTextField(12);
        formGrid.add(txtVehicleNumber, gbc);

        // 2. Vehicle Category
        gbc.gridx = 0; gbc.gridy = 1;
        formGrid.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        comboCategory = new JComboBox<VehicleCategory>(VehicleCategory.values());
        formGrid.add(comboCategory, gbc);

        // 3. Lane Selection
        gbc.gridx = 0; gbc.gridy = 2;
        formGrid.add(new JLabel("Toll Lane:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        comboLane = new JComboBox<String>();
        for (TollLane lane : tollService.getLanes()) {
            comboLane.addItem("Lane " + lane.getLaneId() + (lane.isTagOnly() ? " (Tag Only)" : " (Hybrid)"));
        }
        formGrid.add(comboLane, gbc);

        // 4. Payment Mode (Radio Buttons)
        gbc.gridx = 0; gbc.gridy = 3;
        formGrid.add(new JLabel("Payment Mode:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        radioCash = new JRadioButton("Cash");
        radioTag = new JRadioButton("Toll Tag", true);
        paymentGroup = new ButtonGroup();
        paymentGroup.add(radioCash);
        paymentGroup.add(radioTag);
        radioPanel.add(radioTag);
        radioPanel.add(radioCash);
        formGrid.add(radioPanel, gbc);

        // 5. Toll Tag ID (optional or required for Tag payment)
        gbc.gridx = 0; gbc.gridy = 4;
        formGrid.add(new JLabel("Tag ID (Optional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        txtTagId = new JTextField(12);
        txtTagId.setToolTipText("Enter Tag ID (e.g. TAG101) or leave empty if linked to vehicle number");
        formGrid.add(txtTagId, gbc);

        // 6. Fee Display
        gbc.gridx = 0; gbc.gridy = 5;
        formGrid.add(new JLabel("Applicable Fee:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        lblCalculatedFee = new JLabel("Rs. 100.00");
        lblCalculatedFee.setFont(new Font("Arial", Font.BOLD, 15));
        lblCalculatedFee.setForeground(new Color(39, 174, 96));
        formGrid.add(lblCalculatedFee, gbc);

        // Update fee label on category change
        comboCategory.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    VehicleCategory selected = (VehicleCategory) comboCategory.getSelectedItem();
                    if (selected != null) {
                        lblCalculatedFee.setText("Rs. " + String.format("%.2f", selected.getBaseRate()));
                    }
                }
            }
        });

        // Enable/disable Tag ID field based on radio selection
        radioCash.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtTagId.setEnabled(false);
            }
        });
        radioTag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtTagId.setEnabled(true);
            }
        });

        panel.add(formGrid, BorderLayout.CENTER);

        // Action Buttons at bottom of left panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        btnCollectToll = new JButton("Collect Toll & Generate Receipt");
        btnCollectToll.setBackground(new Color(39, 174, 96));
        btnCollectToll.setFont(new Font("Arial", Font.BOLD, 13));

        btnClearForm = new JButton("Clear Form");

        buttonPanel.add(btnCollectToll);
        buttonPanel.add(btnClearForm);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Button Listeners
        btnCollectToll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCollectToll();
            }
        });

        btnClearForm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearEntryForm();
            }
        });

        return panel;
    }

    // Right Panel: Search, Sort, Table, and Summary Tabs
    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // Top of Right: Search and Sort Toolbar
        JPanel toolbarPanel = new JPanel(new GridLayout(2, 1, 4, 4));

        // Search Bar Row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        searchRow.add(new JLabel("Search Vehicle:"));
        txtSearchVehicle = new JTextField(9);
        searchRow.add(txtSearchVehicle);
        btnSearchVehicle = new JButton("Search");
        searchRow.add(btnSearchVehicle);

        searchRow.add(new JLabel("Filter Lane:"));
        comboFilterLane = new JComboBox<String>();
        comboFilterLane.addItem("All Lanes");
        for (TollLane l : tollService.getLanes()) {
            comboFilterLane.addItem("Lane " + l.getLaneId());
        }
        searchRow.add(comboFilterLane);
        btnFilterLane = new JButton("Filter");
        searchRow.add(btnFilterLane);

        btnResetSearch = new JButton("Reset");
        searchRow.add(btnResetSearch);

        toolbarPanel.add(searchRow);

        // Sort Bar Row
        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        sortRow.add(new JLabel("Sort By:"));
        btnSortAmountAsc = new JButton("Amount (Low -> High)");
        btnSortAmountDesc = new JButton("Amount (High -> Low)");
        btnSortTime = new JButton("Time (Latest First)");
        btnDeleteTx = new JButton("Delete Selected");
        btnDeleteTx.setForeground(Color.RED);

        sortRow.add(btnSortAmountAsc);
        sortRow.add(btnSortAmountDesc);
        sortRow.add(btnSortTime);
        sortRow.add(btnDeleteTx);

        toolbarPanel.add(sortRow);
        panel.add(toolbarPanel, BorderLayout.NORTH);

        // Table Setup
        String[] columns = {"TX ID", "Vehicle No", "Category", "Lane", "Fee (Rs)", "Mode", "Date & Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            // Make table cells non-editable directly in UI
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(24);
        transactionTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        transactionTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane tableScrollPane = new JScrollPane(transactionTable);

        // Bottom of Right: Tabs for Summary Reports
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Live Transactions Table
        tabbedPane.addTab("Transaction Records", tableScrollPane);

        // Tab 2: Lane-wise Summary
        txtLaneSummary = new JTextArea();
        txtLaneSummary.setEditable(false);
        txtLaneSummary.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tabbedPane.addTab("Lane Analytics", new JScrollPane(txtLaneSummary));

        // Tab 3: Category Summary
        txtCategorySummary = new JTextArea();
        txtCategorySummary.setEditable(false);
        txtCategorySummary.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tabbedPane.addTab("Category Breakdown", new JScrollPane(txtCategorySummary));

        panel.add(tabbedPane, BorderLayout.CENTER);

        // Wire Event Listeners for Search / Sort / Delete
        btnSearchVehicle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearchVehicle();
            }
        });

        btnFilterLane.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFilterLane();
            }
        });

        btnResetSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearchVehicle.setText("");
                comboFilterLane.setSelectedIndex(0);
                refreshTableData(tollService.getAllTransactions());
            }
        });

        btnSortAmountAsc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTableData(tollService.getTransactionsSortedByAmountAscending());
            }
        });

        btnSortAmountDesc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTableData(tollService.getTransactionsSortedByAmountDescending());
            }
        });

        btnSortTime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTableData(tollService.getTransactionsSortedByTime(true));
            }
        });

        btnDeleteTx.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeleteTransaction();
            }
        });

        return panel;
    }

    // Bottom Summary Banner
    private JPanel createBottomSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        panel.setBackground(new Color(245, 247, 250));

        lblTotalVehicles = new JLabel("Total Vehicles Processed: 0");
        lblTotalVehicles.setFont(new Font("Arial", Font.BOLD, 13));

        lblTotalCollection = new JLabel("Total Revenue: Rs. 0.00", JLabel.RIGHT);
        lblTotalCollection.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalCollection.setForeground(new Color(41, 128, 185));

        panel.add(lblTotalVehicles);
        panel.add(lblTotalCollection);
        return panel;
    }

    // Handle Collect Toll Action
    private void handleCollectToll() {
        String vehicleNumber = txtVehicleNumber.getText().trim();
        VehicleCategory category = (VehicleCategory) comboCategory.getSelectedItem();
        int laneIndex = comboLane.getSelectedIndex();
        int laneNumber = laneIndex + 1; // Lanes are 1-indexed

        String paymentMode = radioTag.isSelected() ? "TAG" : "CASH";
        String tagId = txtTagId.getText().trim();

        try {
            // Process in Service layer
            Transaction tx = tollService.processToll(vehicleNumber, category, laneNumber, paymentMode, tagId);

            // Success dialog with receipt
            String receipt = "=== TOLL COLLECTION RECEIPT ===\n"
                    + "Receipt ID: " + tx.getTransactionId() + "\n"
                    + "Vehicle No: " + tx.getVehicleNumber() + "\n"
                    + "Category  : " + tx.getVehicleCategory() + "\n"
                    + "Lane No   : " + tx.getLaneNumber() + "\n"
                    + "Fee Paid  : Rs. " + tx.getTollAmount() + "\n"
                    + "Payment   : " + tx.getPaymentMode() + "\n"
                    + "Date/Time : " + tx.getFormattedTime() + "\n"
                    + "Status    : " + tx.getStatus() + "\n"
                    + "===============================";

            JOptionPane.showMessageDialog(this, receipt, "Toll Collected Successfully", JOptionPane.INFORMATION_MESSAGE);

            // Refresh UI
            clearEntryForm();
            refreshTableData(tollService.getAllTransactions());
            updateSummaryBanner();

        } catch (InvalidVehicleException ive) {
            JOptionPane.showMessageDialog(this, ive.getMessage(), "Vehicle Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidPaymentException ipe) {
            JOptionPane.showMessageDialog(this, ipe.getMessage(), "Payment / Tag Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Handle Search Vehicle
    private void handleSearchVehicle() {
        String vNum = txtSearchVehicle.getText().trim();
        if (vNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a vehicle number to search.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ArrayList<Transaction> results = tollService.searchByVehicleNumber(vNum);
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found for vehicle: " + vNum, "No Match", JOptionPane.INFORMATION_MESSAGE);
        }
        refreshTableData(results);
    }

    // Handle Filter Lane
    private void handleFilterLane() {
        int selectedIndex = comboFilterLane.getSelectedIndex();
        if (selectedIndex == 0) {
            // All lanes
            refreshTableData(tollService.getAllTransactions());
        } else {
            int laneNumber = selectedIndex;
            ArrayList<Transaction> results = tollService.searchByLane(laneNumber);
            refreshTableData(results);
        }
    }

    // Handle Delete Transaction (CRUD Delete)
    private void handleDeleteTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction record from the table to delete.", "Select Record", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String txId = (String) tableModel.getValueAt(selectedRow, 0);
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete transaction #" + txId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            boolean deleted = tollService.deleteTransaction(txId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Transaction " + txId + " deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshTableData(tollService.getAllTransactions());
                updateSummaryBanner();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Refresh Table Data
    private void refreshTableData(List<Transaction> list) {
        tableModel.setRowCount(0); // clear existing rows
        for (Transaction tx : list) {
            Object[] row = {
                    tx.getTransactionId(),
                    tx.getVehicleNumber(),
                    tx.getVehicleCategory(),
                    tx.getLaneNumber(),
                    String.format("%.2f", tx.getTollAmount()),
                    tx.getPaymentMode(),
                    tx.getFormattedTime(),
                    tx.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    // Update Bottom Summary Labels & Analytics Text Areas
    private void updateSummaryBanner() {
        int count = tollService.getAllTransactions().size();
        double total = tollService.getTotalPlazaCollection();

        lblTotalVehicles.setText("Total Vehicles Processed: " + count);
        lblTotalCollection.setText(String.format("Total Plaza Revenue: Rs. %.2f", total));

        // Update Lane Analytics tab
        StringBuilder sbLane = new StringBuilder();
        sbLane.append("=== TOLL PLAZA LANE ANALYTICS ===\n\n");
        Map<Integer, Double> laneMap = tollService.getLaneWiseCollection();
        for (TollLane lane : tollService.getLanes()) {
            double coll = laneMap.containsKey(lane.getLaneId()) ? laneMap.get(lane.getLaneId()) : 0.0;
            sbLane.append(String.format("Lane %d: %-25s | Vehicles: %3d | Collection: Rs. %8.2f\n",
                    lane.getLaneId(), lane.getLaneName(), lane.getTransactionCount(), coll));
        }
        txtLaneSummary.setText(sbLane.toString());

        // Update Category Breakdown tab
        StringBuilder sbCat = new StringBuilder();
        sbCat.append("=== VEHICLE CATEGORY REVENUE BREAKDOWN ===\n\n");
        Map<VehicleCategory, Double> catMap = tollService.getCategoryWiseCollection();
        for (VehicleCategory cat : VehicleCategory.values()) {
            sbCat.append(String.format("%-15s (Base Rate: Rs. %3.0f) : Total Revenue = Rs. %8.2f\n",
                    cat.name(), cat.getBaseRate(), catMap.get(cat)));
        }
        txtCategorySummary.setText(sbCat.toString());
    }

    // Clear form inputs
    private void clearEntryForm() {
        txtVehicleNumber.setText("");
        comboCategory.setSelectedIndex(0);
        comboLane.setSelectedIndex(0);
        radioTag.setSelected(true);
        txtTagId.setText("");
        txtTagId.setEnabled(true);
        lblCalculatedFee.setText("Rs. 100.00");
    }

    // Dialog: View Registered Tags
    private void showRegisteredTagsDialog() {
        JDialog dialog = new JDialog(this, "Registered FastTags", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"Tag ID", "Vehicle Number", "Balance (Rs.)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (TollTag tag : tollService.getRegisteredTags().values()) {
            model.addRow(new Object[]{tag.getTagId(), tag.getVehicleNumber(), String.format("%.2f", tag.getBalance())});
        }
        JTable table = new JTable(model);
        dialog.add(new JScrollPane(table));
        dialog.setVisible(true);
    }

    // Dialog: Recharge Tag
    private void showRechargeTagDialog() {
        String tagId = JOptionPane.showInputDialog(this, "Enter Toll Tag ID to recharge (e.g., TAG101):", "Recharge Tag", JOptionPane.QUESTION_MESSAGE);
        if (tagId == null || tagId.trim().isEmpty()) return;

        TollTag tag = tollService.getTagById(tagId);
        if (tag == null) {
            JOptionPane.showMessageDialog(this, "No Toll Tag found with ID: " + tagId, "Tag Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amountStr = JOptionPane.showInputDialog(this, "Current Balance: Rs. " + tag.getBalance() + "\nEnter Recharge Amount (Rs.):", "Recharge Amount", JOptionPane.QUESTION_MESSAGE);
        if (amountStr == null || amountStr.trim().isEmpty()) return;

        try {
            double amt = Double.parseDouble(amountStr.trim());
            tag.recharge(amt);
            JOptionPane.showMessageDialog(this, "Recharge Successful! New Balance: Rs. " + tag.getBalance(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number entered for amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidPaymentException ipe) {
            JOptionPane.showMessageDialog(this, ipe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Dialog: Show Audit Report
    private void showAuditReportDialog() {
        JDialog dialog = new JDialog(this, "Toll Plaza Daily Audit Report", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setText(tollService.generateTollReportText());

        dialog.add(new JScrollPane(area));
        dialog.setVisible(true);
    }
}
