package hospital.resource;

import hospital.common.TriageEvent;
import java.sql.Connection; // Corrected import
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
                    "level TEXT NOT NULL," + // Added missing level column
                    "handled_by TEXT," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" + // Fixed DATETIME
                    ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("DB Initialization error: " + e.getMessage());
        }
    }

    public void savePatient(TriageEvent event) {
        String sql = "INSERT INTO patients(name, level, handled_by) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Cast removed
            pstmt.setString(1, event.getPatientName());
            pstmt.setString(2, event.getTriageLevel());
            pstmt.setString(3, event.getHandledBy());
            pstmt.executeUpdate();
            System.out.println("Patient saved to permanent storage.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}