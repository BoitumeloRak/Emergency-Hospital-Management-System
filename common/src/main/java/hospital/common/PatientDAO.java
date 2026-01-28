package hospital.common;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Will handle the connection and the SQL queries
 */

public class PatientDAO {
    private static final String URL = "jdbc:sqlite:hospital.db";

    public PatientDAO() {
        // CONSTRUCTOR
        // Automatically create the table when the class is initialised
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "level TEXT NOT NULL," +
                    "category TEXT," +
                    "age INTEGER," +
                    "ward TEXT," +
                    "handled_by TEXT," +
                    "bed_number TEXT," +
                    "status TEXT DEFAULT 'ADMITTED', " + // ADMITTED, TRANSFERRED, DISCHARGED
                    "destination TEXT DEFAULT 'NONE', " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("DB Initialization error: " + e.getMessage());
        }
    }

    public void savePatient(TriageEvent event, String assignedWard, String status, String destination) {
        // Match teh columns to CREATE TABLE statement
        String sql = "INSERT INTO patients(name, level, category, age, ward, handled_by, status, destination) VALUES(?,?,?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, event.getPatientName());
            pstmt.setString(2, event.getTriageLevel());
            pstmt.setString(3, event.getPatientCategory()); // saving category
            pstmt.setInt(4, event.getAge()); // save age
            //pstmt.setString(5, assignedWard); // ward determined
            pstmt.setString(5, assignedWard); // assignedWard
            pstmt.setString(6, event.getHandledBy());
            pstmt.setString(7, status); // "ADMITTED" or "TRANSFERRED"
            pstmt.setString(8, destination); // "NONE" or "CHRIS HANI BARA"

            pstmt.executeUpdate();
            System.out.println("Patient saved to " + assignedWard);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error saving patient: " + e.getMessage());
        }
    }

    public List<Map<String, String>> getHistory() {
        List<Map<String, String>> history = new ArrayList<>();

        // Order by timestamp so the most recent patients appear at the top
        String sql = "SELECT * FROM patients ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // We use Map.of to create a clean "JSON-ready" object for each row
                history.add(Map.of(
                        "name", rs.getString("name"),
                        "level", rs.getString("level"),
                        "category", rs.getString("category") != null ? rs.getString("category") : "General",
                        "age", String.valueOf(rs.getInt("age")),
                        "ward", rs.getString("ward") != null ? rs.getString("ward") : "Unassigned",
                        "time", rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // Counts the active patients in ward
    public int getWardCount(String wardName) {
        String sql = "SELECT COUNT(*) FROM patients WHERE ward = ? AND status = 'ADMITTED'";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, wardName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if error
    }
}

/*
We need to keep the data separate from the decisions
 */