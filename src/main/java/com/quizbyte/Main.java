package com.quizbyte;

import com.quizbyte.config.DatabaseConfig;
import com.quizbyte.ui.MainFrame;

import javax.swing.*;

/**
 * Quizbyte - Interactive Desktop Online Quiz Platform
 * Application entry point launching the modern Java Swing GUI on the Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {
        // Enable OS native look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Main] Notice: Using fallback Swing look-and-feel: " + e.getMessage());
        }

        // Asynchronous database connectivity diagnostic
        new Thread(() -> {
            if (!DatabaseConfig.testConnection()) {
                System.err.println("[DatabaseConfig] Warning: Could not connect to MySQL at: " + DatabaseConfig.getUrl());
                System.err.println("                 Please verify MySQL80 service is active and schema.sql has been executed.");
            } else {
                System.out.println("[DatabaseConfig] MySQL Database connected successfully.");
            }
        }).start();

        // Launch the graphical interface on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
