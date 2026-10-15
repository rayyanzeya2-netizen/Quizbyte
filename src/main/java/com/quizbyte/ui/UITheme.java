package com.quizbyte.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Central styling and design system constants for Quizbyte GUI.
 * Standardizes button visibility, accessible high-contrast palettes,
 * tactile mouse hover effects, and clean table formatting.
 */
public class UITheme {

    // Primary & Accent Color Palette
    public static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);         // Vibrant Royal Blue
    public static final Color PRIMARY_DARK = new Color(0x1D, 0x4E, 0xD8);    // Deep Blue Hover
    public static final Color PRIMARY_LIGHT = new Color(0xDB, 0xEA, 0xFE);   // Soft Ice Blue

    public static final Color SLATE_DARK = new Color(0x1E, 0x29, 0x3B);      // Slate 800
    public static final Color SLATE_PANEL = new Color(0x33, 0x41, 0x55);     // Slate 700
    public static final Color SLATE_BTN = new Color(0x47, 0x55, 0x69);       // Slate 600
    public static final Color SLATE_BTN_HOVER = new Color(0x33, 0x41, 0x55); // Slate 700 Hover

    public static final Color BG_LIGHT = new Color(0xF8, 0xFA, 0xFC);        // Clean Off-White
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(0xCB, 0xD5, 0xE1);    // Slate 300 Border

    public static final Color TEXT_MAIN = new Color(0x0F, 0x17, 0x2A);       // Dark Slate Text
    public static final Color TEXT_MUTED = new Color(0x47, 0x55, 0x69);      // Muted Slate Text

    public static final Color SUCCESS = new Color(0x05, 0x96, 0x69);         // Emerald Green
    public static final Color SUCCESS_DARK = new Color(0x04, 0x78, 0x57);

    public static final Color DANGER = new Color(0xDC, 0x26, 0x26);          // Crimson Red
    public static final Color DANGER_DARK = new Color(0xB9, 0x1C, 0x1C);

    public static final Color WARNING = new Color(0xD9, 0x77, 0x06);         // Amber 600

    // Typography
    public static final Font FONT_HEADER_LARGE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SCORE_BADGE = new Font("Segoe UI", Font.BOLD, 48);

    private UITheme() {}

    /**
     * Creates a high-contrast Primary action button (e.g. Start Quiz, Submit, Sign In).
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY, Color.WHITE, PRIMARY_DARK, new EmptyBorder(10, 18, 10, 18));
    }

    /**
     * Creates a high-contrast Success action button (e.g. Save Question, Create Account).
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS, Color.WHITE, SUCCESS_DARK, new EmptyBorder(10, 18, 10, 18));
    }

    /**
     * Creates a high-contrast Secondary / Utility button (e.g. Refresh, Retake, Back).
     * Solves washed-out appearance with solid Slate background and white readable text.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(SLATE_BTN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(SLATE_DARK, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SLATE_BTN_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SLATE_BTN);
            }
        });
        return button;
    }

    /**
     * Creates a high-contrast Danger / Destructive button (e.g. Log Out, Cancel, Quit).
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER, Color.WHITE, DANGER_DARK, new EmptyBorder(8, 16, 8, 16));
    }

    private static JButton createStyledButton(String text, Color bg, Color fg, Color hoverBg, EmptyBorder padding) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(padding);
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverBg);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    /**
     * Creates a styled text field with rounded padding.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        styleInputComponent(tf);
        return tf;
    }

    /**
     * Creates a styled password field.
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        styleInputComponent(pf);
        return pf;
    }

    private static void styleInputComponent(JComponent comp) {
        comp.setFont(FONT_BODY);
        comp.setBackground(Color.WHITE);
        comp.setForeground(TEXT_MAIN);
        comp.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    /**
     * Styles a JTable with high-contrast header, centered columns, and generous row height.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30); // Meets minimum 28px requirement
        table.setShowGrid(true);
        table.setGridColor(new Color(0xE2, 0xE8, 0xF0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_MAIN);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(SLATE_DARK);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        // Center align table headers
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Center align table contents by default
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }

    /**
     * Creates a clean placeholder panel displayed when a table is empty.
     */
    public static JPanel createEmptyPlaceholder(String message) {
        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(Color.WHITE);
        placeholder.setBorder(new EmptyBorder(40, 20, 40, 20));

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(FONT_BODY_BOLD);
        label.setForeground(TEXT_MUTED);

        placeholder.add(label);
        return placeholder;
    }

    /**
     * Wraps a component inside a card-style JPanel with subtle border and white background.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));
        return card;
    }
}
