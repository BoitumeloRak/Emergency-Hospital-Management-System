package hospital.common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * The TriageEvent is the "Message" sent across the system
 * It must implement Serializable so ActiveMQ can "pack" it for travel
 */

public class TriageEvent implements Serializable {

    private String patientName;
    private String triageLevel; // e.g. RED, ORANGE, YELLOW, GREEN
    private String handleBy;
    private String timestamp;

    // Default constructor (required for JSON/Jackson)
    public TriageEvent() {}

    public TriageEvent(String patientName, String triageLevel) {
        this.patientName = patientName;
        this.triageLevel = triageLevel;
        this.timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters and Setters
    public String getPatientName() {return patientName; }
    public void setPatientName(String patientName) {this.patientName = patientName; }
    public String getTriageLevel() {return triageLevel; }
    public void setTriageLevel(String triageLevel) {this.triageLevel = triageLevel; }
    public String getTimestamp() {return timestamp; }
    public void setHandleBy(String handleBy) { this.handleBy = handleBy; }
    public void setTimestamp(String timestamp) {this.timestamp = timestamp; }

    public String getHandledBy() {
        return (handleBy == null || handleBy.isEmpty()) ? "Unknown staff" : handleBy;
    }
}
