package hospital.common;

import java.io.Serializable;

/*
 * The TriageEvent is the "Message" sent across the system
 * It must implement Serializable so ActiveMQ can "pack" it for travel
 */

public class TriageEvent implements Serializable {

    private String paitentName;
    private String triageLevel; // e.g. RED, ORANGE, YELLOW, GREEN
    private long timestamp;

    // Default constructor (required for JSON/Jackson)
    public TriageEvent() {}

    public TriageEvent(String paitentName, String triageLevel) {
        this.paitentName = paitentName;
        this.triageLevel = triageLevel;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getPaitentName() {return paitentName; }
    public void setPaitentName(String paitentName) {this.paitentName = paitentName; }
    public String getTriageLevel() {return triageLevel; }
    public void setTriageLevel(String triageLevel) {this.triageLevel = triageLevel; }
    public long getTimestamp() {return timestamp; }
    public void setTimestamp(long timestamp) {this.timestamp = timestamp; }
}
