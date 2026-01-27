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
        // Automatically create the table when the class is initialised
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "level TEXT NOT NULL," +
                    "handled_by TEXT," +
                    "bed_number TEXT," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("DB Initialization error: " + e.getMessage());
        }
    }

    public void savePatient(TriageEvent event, String bedNumber) {
        String sql = "INSERT INTO patients(name, level, handled_by) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Cast removed
            pstmt.setString(1, event.getPatientName());
            pstmt.setString(2, event.getTriageLevel());
            pstmt.setString(3, event.getHandledBy());
            pstmt.setString(4, bedNumber);
            pstmt.executeUpdate();
            System.out.println("Patient saved to permanent storage.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, String>> getHistory() {
        List<Map<String, String>> history = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                history.add(Map.of(
                        "name", rs.getString("name"),
                        "level", rs.getString("level"),
                        "bed", rs.getString("bed_number"),
                        "time", rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}