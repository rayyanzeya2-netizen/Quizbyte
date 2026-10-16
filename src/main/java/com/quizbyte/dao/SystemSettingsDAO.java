package com.quizbyte.dao;

import com.quizbyte.config.DatabaseConfig;
import com.quizbyte.exception.DatabaseOperationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for system-wide configuration parameters (system_settings).
 */
public class SystemSettingsDAO {

    /**
     * Retrieves the string value for a setting key, returning defaultValue if not found.
     */
    public String getSetting(String key, String defaultValue) throws DatabaseOperationException {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching setting: " + key, e);
        }
        return defaultValue;
    }

    /**
     * Updates or inserts a setting key-value pair.
     */
    public boolean updateSetting(String key, String value) throws DatabaseOperationException {
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            stmt.setString(2, value);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating setting: " + key, e);
        }
    }

    /**
     * Retrieves all system settings as a Key-Value Map.
     */
    public Map<String, String> getAllSettings() throws DatabaseOperationException {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM system_settings";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all system settings", e);
        }
        return settings;
    }
}
