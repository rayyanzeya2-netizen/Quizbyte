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
 * Shared UI styling utility for Quizbyte.
 * Solves Look & Feel button color overrides (white-on-white) and table header contrast
 * by using BasicButtonUI and custom TableCellRenderers.
 */
public class UIUtils {

    // Central Color Tokens
    public static final Color COLOR_PRIMARY = new Color(0x25, 0x63, 0xEB);         // Vibrant Royal Blue
    public static final Color COLOR_PRIMARY_HOVER = new Color(0x1D, 0x4E, 0xD8);   // Darker Blue
    public static final Color COLOR_SECONDARY = new Color(0x47, 0x55, 0x69);       // Solid Slate / Cool Gray
    public static final Color COLOR_SECONDARY_HOVER = new Color(0x33, 0x41, 0x55); // Dark Slate Hover
    public static final Color COLOR_DANGER = new Color(0xDC, 0x26, 0x26);          // Crimson Red
    public static final Color COLOR_DANGER_HOVER = new Color(0xB9, 0x1C, 0x1C);
    public static final Color COLOR_SUCCESS = new Color(0x05, 0x96, 0x69);         // Emerald Green
    public static final Color COLOR_SUCCESS_HOVER = new Color(0x04, 0x78, 0x57);
    public static final Color COLOR_HEADER_BG = new Color(0x1E, 0x29, 0x3B);       // Dark Navy Slate
    public static final Color COLOR_BORDER = new Color(0xCB, 0xD5, 0xE1);

    public static final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);

    private UIUtils() {}

    /**
     * Styles an existing or newly created JButton with explicit BasicButtonUI to prevent
     * Windows native Look and Feel from discarding custom background colors.
     */
    public static JButton styleButton(JButton button, Color bg, Color fg, Color hoverBg, Insets padding) {
        // Enforce BasicButtonUI so Windows Look & Feel does not override background with white texture
        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(FONT_BTN);
        button.setBackground(bg);
        button.setForeground(fg);

        button.setBorder(new CompoundBorder(
                new LineBorder(hoverBg, 1, true),
                new EmptyBorder(padding.top, padding.left, padding.bottom, padding.right)
        ));

        // Add tactile mouse hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
                button.repaint();
            }
        });

        return button;
    }

    /**
     * Creates a high-contrast Primary button (Vibrant Blue with White text).
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        return styleButton(btn, COLOR_PRIMARY, Color.WHITE, COLOR_PRIMARY_HOVER, new Insets(10, 20, 10, 20));
    }

    /**
     * Creates a high-contrast Secondary / Utility button.
     * Solves the white-on-white text bug by applying a distinct solid cool gray (#475569)
     * and crisp bold white text (#FFFFFF).
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        return styleButton(btn, COLOR_SECONDARY, Color.WHITE, COLOR_SECONDARY_HOVER, new Insets(8, 18, 8, 18));
    }

    /**
     * Creates a high-contrast Danger / Destructive button (Crimson Red with White text).
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        return styleButton(btn, COLOR_DANGER, Color.WHITE, COLOR_DANGER_HOVER, new Insets(8, 16, 8, 16));
    }

    /**
     * Creates a high-contrast Success action button (Emerald Green with White text).
     */
    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        return styleButton(btn, COLOR_SUCCESS, Color.WHITE, COLOR_SUCCESS_HOVER, new Insets(10, 20, 10, 20));
    }

    /**
     * Formats JTable and enforces a custom TableHeaderRenderer so Windows Look and Feel
     * does not paint white headers with white text.
     */
    public static void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xE2, 0xE8, 0xF0));
        table.setSelectionBackground(new Color(0xDB, 0xEA, 0xFE));
        table.setSelectionForeground(new Color(0x0F, 0x17, 0x2A));
        table.setFillsViewportHeight(true);

        // Configure Table Header
        JTableHeader header = table.getTableHeader();
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_HEADER);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);

        // Custom Header Renderer to bypass Windows native L&F skinning
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setOpaque(true);
                label.setBackground(COLOR_HEADER_BG);
                label.setForeground(Color.WHITE);
                label.setFont(FONT_HEADER);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(0x33, 0x41, 0x55)),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
                return label;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        // Center align table body contents
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }
}
