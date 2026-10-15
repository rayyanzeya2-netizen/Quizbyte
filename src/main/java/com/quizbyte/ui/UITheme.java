package com.quizbyte.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Central styling and design system constants for Quizbyte GUI.
 * Guarantees high-contrast readable buttons and dark slate table headers
 * by using BasicButtonUI to prevent Windows native Look and Feel overrides.
 */
public class UITheme {

    // Primary & Accent Color Palette
    public static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);         // Vibrant Royal Blue
    public static final Color PRIMARY_DARK = new Color(0x1D, 0x4E, 0xD8);    // Deep Blue Hover
    public static final Color PRIMARY_LIGHT = new Color(0xDB, 0xEA, 0xFE);   // Soft Ice Blue

    public static final Color SLATE_DARK = new Color(0x1E, 0x29, 0x3B);      // Slate 800 (Header Navy)
    public static final Color SLATE_PANEL = new Color(0x33, 0x41, 0x55);     // Slate 700
    public static final Color SLATE_BTN = new Color(0x47, 0x55, 0x69);       // Solid Slate 600
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
        return UIUtils.createPrimaryButton(text);
    }

    /**
     * Creates a high-contrast Success action button (e.g. Save Question, Create Account).
     */
    public static JButton createSuccessButton(String text) {
        return UIUtils.createSuccessButton(text);
    }

    /**
     * Creates a high-contrast Secondary / Utility button (e.g. Refresh, Retake, Back).
     * Solves the white-on-white text bug by applying solid cool gray (#475569) and white bold text.
     */
    public static JButton createSecondaryButton(String text) {
        return UIUtils.createSecondaryButton(text);
    }

    /**
     * Creates a high-contrast Danger / Destructive button (e.g. Log Out, Cancel, Quit).
     */
    public static JButton createDangerButton(String text) {
        return UIUtils.createDangerButton(text);
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
        UIUtils.styleTable(table);
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
