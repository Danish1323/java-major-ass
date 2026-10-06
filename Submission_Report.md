# Toll Plaza Fee Collection & Management System
## Java Programming — Case Study 87 | Project Submission Report

---

## 1. Problem Statement

A highway toll plaza needs a system to manage vehicle categories, calculate toll fees, and track daily toll collection across multiple lanes. The system should support electronic toll tags along with cash payments.

### Objectives:
- Maintain vehicle category and toll rate information
- Calculate toll fees per vehicle type
- Track lane-wise daily collections
- Support electronic tag and cash payments
- Search and sort toll transaction records
- Provide a toll management GUI

---

## 2. Proposed Solution

We developed a complete **desktop-based Toll Plaza Fee Collection & Management System** using **Core Java and Java Swing**. The application allows a toll booth operator to:

1. Select a vehicle's registration number, category (CAR, TRUCK, BUS, TWO_WHEELER), and toll lane
2. Choose between Cash payment or Electronic Toll Tag (FASTag) payment
3. Automatically calculate the toll fee based on vehicle category
4. Deduct balance from the FASTag if electronic payment is selected
5. Store every transaction and persist it to a text file
6. Search transactions by vehicle number or filter by lane
7. Sort transactions by toll amount (ascending/descending) or by timestamp
8. Delete transactions from records
9. View lane-wise analytics and category-wise revenue breakdown
10. Generate a formatted daily audit report

---

## 3. System Architecture

The project follows a layered architecture:

```
┌─────────────────────────────────────────────────┐
│                  GUI Layer                       │
│         (TollPlazaGUI.java - Java Swing)         │
├─────────────────────────────────────────────────┤
│               Service Layer                      │
│    (TollService.java + Validator.java)           │
├─────────────────────────────────────────────────┤
│                Model Layer                       │
│  Vehicle, StandardVehicle, VehicleCategory,      │
│  TollTag, TollLane, Transaction                  │
├─────────────────────────────────────────────────┤
│             Exception Layer                      │
│  InvalidVehicleException, InvalidPaymentException│
├─────────────────────────────────────────────────┤
│             Persistence Layer                    │
│        (File I/O: toll_transactions.txt)         │
└─────────────────────────────────────────────────┘
```

---

## 4. Project Structure

```
Java-project/
├── src/
│   └── com/
│       └── tollplaza/
│           ├── Main.java                          (Application entry point)
│           ├── model/
│           │   ├── VehicleCategory.java           (Enum: CAR, TRUCK, BUS, TWO_WHEELER)
│           │   ├── Vehicle.java                   (Abstract base class)
│           │   ├── StandardVehicle.java           (Concrete subclass of Vehicle)
│           │   ├── TollTag.java                   (Electronic toll tag / FASTag)
│           │   ├── TollLane.java                  (Toll lane with LinkedList history)
│           │   └── Transaction.java               (Toll event record)
│           ├── exception/
│           │   ├── InvalidVehicleException.java   (Custom checked exception)
│           │   └── InvalidPaymentException.java   (Custom checked exception)
│           ├── service/
│           │   ├── TollService.java               (Core business logic & CRUD)
│           │   └── Validator.java                 (Input validation utility)
│           └── gui/
│               └── TollPlazaGUI.java              (Complete Swing desktop GUI)
├── bin/                                           (Compiled .class files)
└── toll_transactions.txt                          (Persistent data file)
```

---

## 5. Java Concepts Used & Where They Are Applied

This section maps every required Java concept from the syllabus to its actual implementation in the project.

### 5.1 Classes and Objects
We created 6 classes to model real-world entities:
- **Vehicle** — Abstract base class representing any vehicle at the toll gate
- **StandardVehicle** — Concrete subclass that inherits from Vehicle
- **TollTag** — Represents an electronic prepaid tag (like FASTag) with balance
- **TollLane** — Represents a physical lane at the toll plaza
- **Transaction** — Records each toll collection event with all details
- **TollService** — Service class handling all business logic

### 5.2 Constructors
Constructors are used to initialize objects with their required state as mandated by the problem statement:
- `Vehicle` and `StandardVehicle` initialize vehicle registration number and category. `StandardVehicle` calls `super(registrationNumber, category)`.
- `TollTag` initializes tag ID, associated vehicle number, and initial wallet balance.
- `TollLane` initializes lane ID, name, whether it is a dedicated tag lane, and instantiates the internal `LinkedList<Transaction>`.
- `Transaction` initializes transaction details and auto-assigns the transaction timestamp and status.

### 5.3 Enum
`VehicleCategory` is an enum with four constants: `CAR(100.0)`, `TRUCK(250.0)`, `BUS(200.0)`, `TWO_WHEELER(50.0)`. Each constant stores its own base toll rate and exposes it via the `getBaseRate()` method. This provides type-safe vehicle categorization instead of error-prone string comparisons.

### 5.4 Wrapper Classes
`Transaction.java` uses:
- `Integer laneNumber` instead of primitive `int`
- `Double tollAmount` instead of primitive `double`

These wrapper types enable null-safety, compatibility with Collections (generics), and access to methods like `Double.compareTo()` and `Integer.valueOf()`.

### 5.5 ArrayList
`TollService` stores all transactions in an `ArrayList<Transaction>`. This dynamic resizable array provides:
- Fast O(1) indexed access for populating the JTable
- Flexible size — grows automatically as vehicles pass through

### 5.6 LinkedList
Each `TollLane` object maintains a `LinkedList<Transaction>` to record the chronological sequence of vehicles that crossed that lane. LinkedList provides O(1) insertion at the tail, making it ideal for sequential event logs.

### 5.7 HashMap
`TollService` maintains a `HashMap<String, ArrayList<Transaction>>` mapping each vehicle registration number to its list of transactions. This enables instant O(1) average-time lookups when searching by vehicle number.

### 5.8 TreeMap
`TollService` maintains a `TreeMap<Double, ArrayList<Transaction>>` where the key is the toll amount. Since TreeMap is backed by a Red-Black self-balancing binary search tree, entries are automatically kept in ascending sorted order by key, enabling efficient sorted-by-amount queries without explicit sorting.

### 5.9 CRUD Operations
All four CRUD operations are implemented in `TollService`:
- **Create**: `processToll()` validates inputs, calculates fee, deducts tag balance (if applicable), creates a Transaction, and saves it to all data structures and the file
- **Read**: `getAllTransactions()`, `searchByVehicleNumber()`, `searchByLane()`, `findTransactionById()`
- **Update**: `updateTransaction()` modifies lane number and payment mode of an existing transaction
- **Delete**: `deleteTransaction()` removes a record from ArrayList, HashMap, and TreeMap, then saves to file

### 5.10 Searching
Two search strategies are implemented:
- **Hash-based search**: `searchByVehicleNumber()` uses HashMap for O(1) lookup
- **Linear search**: `searchByLane()` iterates through the ArrayList filtering by lane number

### 5.11 Sorting
Three sorting approaches are demonstrated:
1. **Natural ordering via TreeMap**: Iterating `amountSortedMap.entrySet()` yields transactions in ascending fee order automatically
2. **Comparator (descending amount)**: Anonymous `Comparator<Transaction>` class reverses `Double.compareTo()` for high-to-low sorting
3. **Comparator (by timestamp)**: Another anonymous Comparator sorts by `Date.compareTo()` for chronological ordering

Additionally, `Transaction` implements `Comparable<Transaction>` with `compareTo()` for natural ordering by toll amount.

### 5.12 Swing GUI
`TollPlazaGUI` is a full-featured desktop interface using:
- **JFrame** — Main application window
- **JPanel** — Container panels for organizing sections
- **JSplitPane** — Splits the window into a left form panel and right data panel
- **JTabbedPane** — Tabs for "Transaction Records", "Lane Analytics", and "Category Breakdown"
- **JTable with DefaultTableModel** — Displays transaction data in a spreadsheet format
- **JTextField** — Text input fields for vehicle number, tag ID, and search
- **JComboBox** — Dropdown selectors for vehicle category, lane, and lane filter
- **JRadioButton with ButtonGroup** — Mutually exclusive Cash/Tag payment mode selection
- **JButton** — Action buttons for collecting toll, searching, sorting, deleting, and clearing
- **JLabel** — Display labels for fee amount and summary totals
- **JTextArea with JScrollPane** — Scrollable text areas for analytics reports
- **JMenuBar, JMenu, JMenuItem** — File menu (Save, Generate Report, Exit), Electronic Tags menu (View Tags, Recharge), Help menu (About)
- **JDialog** — Custom modal dialogs for viewing registered tags and audit reports
- **JOptionPane** — Information, Warning, Error, Confirmation, and Input dialog boxes

**Layout Managers used:**
- **BorderLayout** — Main frame layout (NORTH header, CENTER split pane, SOUTH summary bar)
- **GridBagLayout** — Left entry form with aligned label-field pairs
- **GridLayout** — Button panels and summary bar
- **FlowLayout** — Search/sort toolbar rows and radio button panel

### 5.13 Exception Handling
Two custom checked exceptions extend `java.lang.Exception`:
- **InvalidVehicleException** — Thrown when vehicle registration number is empty, too short/long, or contains special characters
- **InvalidPaymentException** — Thrown when tag balance is insufficient, recharge amount is negative, payment mode is invalid, or no tag is found

These exceptions propagate from the Validator and TollTag up through TollService and are caught in the GUI with `try-catch` blocks, displaying user-friendly error dialogs. File I/O operations use `try-catch-finally` to ensure streams are properly closed.

### 5.14 Validation
`Validator.java` provides four static validation methods:
- `validateVehicleNumber()` — Checks null, empty, length (4-15 chars), and character validity using a for loop with `Character.isLetterOrDigit()`
- `validateTagId()` — Checks null, empty, and length bounds
- `validateFeeAmount()` — Checks null, positive value, and maximum limit using `Double` wrapper
- `validatePaymentMode()` — Ensures mode is strictly "CASH" or "TAG"

### 5.15 Abstract Classes and Inheritance
- `Vehicle` is declared as `abstract class` with an abstract method `calculateTollFee()`
- `StandardVehicle extends Vehicle` provides a concrete implementation of `calculateTollFee()` using `getCategory().getBaseRate()`
- Constructors in `StandardVehicle` use `super()` to call parent constructors

### 5.16 Encapsulation and Access Modifiers
All model classes follow strict encapsulation:
- All fields are declared `private`
- Access is provided through `public` getter and setter methods
- Business methods like `TollTag.deductAmount()` and `TollTag.recharge()` contain validation logic that protects internal state

### 5.17 Method Overriding and Polymorphism
- `toString()` is overridden in all model classes (`Vehicle`, `TollTag`, `TollLane`, `Transaction`) to provide readable string representations
- `calculateTollFee()` is overridden in `StandardVehicle` (Runtime Polymorphism)
- `compareTo()` is overridden in `Transaction` to implement the `Comparable` interface
- Multiple constructors in each class demonstrate Method/Constructor Overloading (Compile-time Polymorphism)

### 5.18 File I/O
`TollService` implements persistent storage:
- **Writing**: `BufferedWriter` wrapping `FileWriter` writes CSV-formatted transaction records to `toll_transactions.txt`
- **Reading**: `BufferedReader` wrapping `FileReader` reads the file line-by-line using `readLine()`, splits each line using `String.split(",")`, and reconstructs Transaction objects
- **Error handling**: Both operations are wrapped in `try-catch-finally` blocks to ensure streams are closed even if exceptions occur

### 5.19 Multithreading
`Main.java` uses `SwingUtilities.invokeLater(new Runnable() { ... })` to launch the GUI on the Event Dispatch Thread (EDT). This is the standard practice for Swing applications because Swing components are not thread-safe and all UI operations must execute on the EDT.

---

## 6. Expected Modules — Implementation Mapping

| Expected Module | How It Is Implemented |
| :--- | :--- |
| 1. Vehicle Categorization | `VehicleCategory` enum with 4 constants and base rates |
| 2. Fee Calculation | `TollService.calculateFee()` returns rate from enum |
| 3. Lane-wise Collection | `TollLane` objects with `LinkedList` history; lane analytics tab |
| 4. Tag/Cash Processing | `processToll()` handles both modes; deducts from `TollTag` balance |
| 5. Daily Summary | Summary banner + `generateTollReportText()` audit report |
| 6. Transaction Search | `searchByVehicleNumber()` (HashMap) and `searchByLane()` (linear) |
| 7. Toll Reports | Lane analytics tab, Category breakdown tab, Audit report dialog |

---

## 7. How to Compile and Run

### Prerequisites
- Java Development Kit (JDK) 8 or above installed
- Terminal / Command Prompt access

### Step 1: Compile
Open terminal in the project root directory and run:
```
javac -d bin src/com/tollplaza/model/*.java src/com/tollplaza/exception/*.java src/com/tollplaza/service/*.java src/com/tollplaza/gui/*.java src/com/tollplaza/Main.java
```

### Step 2: Run
```
java -cp bin com.tollplaza.Main
```
This launches the Swing desktop GUI application.

### Sample Test Data
The system comes pre-loaded with 4 electronic toll tags for testing:

| Tag ID | Vehicle Number | Starting Balance |
| :--- | :--- | :--- |
| TAG101 | MH12AB1234 | Rs. 1000.00 |
| TAG102 | DL01XY9876 | Rs. 500.00 |
| TAG103 | KA05MN4321 | Rs. 80.00 |
| TAG104 | HR26CD5566 | Rs. 1200.00 |

4 toll lanes are configured:
- Lane 1: North Express Lane (FASTag Only)
- Lane 2: North General Lane (Hybrid — Cash & Tag)
- Lane 3: South Express Lane (FASTag Only)
- Lane 4: South General Lane (Hybrid — Cash & Tag)

---

## 8. Screenshots Description

*(Attach actual screenshots from your running application here)*

1. **Main Application Window** — Shows the left entry form, right transaction table, top header banner, and bottom summary bar
2. **Toll Collection Receipt** — JOptionPane dialog showing successful toll collection with receipt details
3. **Search Results** — Table filtered by vehicle number
4. **Lane Analytics Tab** — Displays lane-wise vehicle count and revenue
5. **Category Breakdown Tab** — Shows revenue per vehicle category
6. **Audit Report Dialog** — Formatted daily report accessed via File menu
7. **Error Dialog** — Example of exception handling when entering invalid vehicle data
8. **Registered Tags Dialog** — Table of all FASTag accounts and balances

---

## 9. Conclusion

This project successfully implements a fully functional Toll Plaza Fee Collection and Management System covering all the required Java concepts from the syllabus — including Object-Oriented Programming principles (Encapsulation, Inheritance, Abstraction, Polymorphism), Enumerations, Wrapper Classes, the Java Collections Framework (ArrayList, LinkedList, HashMap, TreeMap), Searching and Sorting algorithms, Custom Exception Handling, File Input/Output Streams, and a complete Java Swing desktop GUI with multiple layout managers, event handling, menus, dialog boxes, and JTable data display.

The system provides a practical, real-world application of these concepts in a highway toll management scenario, demonstrating how Java programming constructs work together to build a complete, functional software system.
