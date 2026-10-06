package com.tollplaza;

import com.tollplaza.gui.TollPlazaGUI;
import com.tollplaza.service.TollService;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main class to launch the Toll Plaza Fee Collection & Management System.
 */
public class Main {
    public static void main(String[] args) {
        // Set cross-platform or system look and feel for neat student GUI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default Swing look and feel
        }

        // Run GUI on Event Dispatch Thread (Swing Multithreading standard practice)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Instantiate the TollService backend
                TollService service = new TollService();

                // Create and display the Swing GUI window
                TollPlazaGUI gui = new TollPlazaGUI(service);
                gui.setVisible(true);
            }
        });
    }
}
