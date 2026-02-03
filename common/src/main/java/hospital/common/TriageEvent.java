package hospital.common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * The TriageEvent is the "Message" sent across the system
 * It must implement Serializable so ActiveMQ can "pack" it for travel
 *
 * Data Model
 * The Message -Think of this as a letter. It only contains facts about the patient
 * The letter doesn't know if the hospital is full
 */

public class TriageEvent implements Serializable {

    // Fields
    private String patientName;
    private String triageLevel; // e.g. RED, ORANGE, YELLOW, GREEN
    private String patientCategory; // e.g. "General", "Maternity", "Pediatric" (ward)
    private String gender;
    private int age;
    private String timestamp;
    private String handledBy;


    // Default constructor (required for JSON/Jackson)
    public TriageEvent() {
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd:mm:ss"));
    }

    // Constructors
    public TriageEvent(String patientName, String triageLevel, String gender, int age, String handledBy, String patientCategory) {
        this();
        this.patientName = patientName;
        this.triageLevel = triageLevel;
        this.handledBy = handledBy;
        this.patientCategory = patientCategory;
        this.age = age;
        this.gender = gender;
       // this.timestamp = java.time.LocalDateTime.now()
         //       .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters and Setters
    public String getPatientName() {return patientName; }
    public  void setPatientName(String patientName) { this.patientName = patientName; }

    public String getTriageLevel() { return triageLevel; }
    public void setTriageLevel(String triageLevel) { this.triageLevel = triageLevel; }

    public String getPatientCategory() { return patientCategory; }
    public void setPatientCategory(String patientCategory) { this.patientCategory = patientCategory; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) {this.age = age; }

    public String getHandledBy() {
        return (handledBy == null || handledBy.isEmpty()) ? "Unknown staff" : handledBy; }
    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }


    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }


}
