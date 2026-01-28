package hospital.common;

import java.io.Serializable;

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
    private String handledBy;
    private String timestamp;
    private String patientCategory; // e.g. "General", "Maternity", "Pediatric" (ward)
    private String gender;
    private int age;


    // Default constructor (required for JSON/Jackson)
    public TriageEvent() {}

    // Constructors
    public TriageEvent(String patientName, String triageLevel, String gender, int age, String handleBy, String patientCategory) {
        this.patientName = patientName;
        this.triageLevel = triageLevel;
        this.handledBy = handleBy;
        this.patientCategory = patientCategory;
        this.age = age;
        this.gender = gender;
        this.timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters and Setters
    public String getPatientName() {return patientName; }
    public String getTriageLevel() { return triageLevel; }
    public String getPatientCategory() { return patientCategory; }
    public int getAge() { return age; }
    public String getHandledBy() {
        return (handledBy == null || handledBy.isEmpty()) ? "Unknown staff" : handledBy; }
    public String getGender() { return gender; }
    public String getTimestamp() { return timestamp; }


}
