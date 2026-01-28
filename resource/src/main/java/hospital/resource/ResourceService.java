package hospital.resource;

import hospital.common.HospitalMQ;
import hospital.common.PatientDAO;
import hospital.common.TriageEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.jms.Connection;

/*
 * This service deos not need a web server (like Javalin) necessarily,
 * but it must stay running so it can listen for messages from ActiveMQ
 * Needs to listen to hospital.triage topic. When I run the curl command, this service should automatically react
 * Needs a MessageListener. Unlike TriageService this service stays open forever, waiting for data
 * "Consumes" the message. It listens for new data and acts on it
 *
 * "Ward Brain" Logic (Brain" of the hospital)
 * The Processor - This is the Hospital Manager. Only the manager knows how many beds are free by looking at the database
 * Receives the TriageEvent
 */
public class ResourceService {
    private static final PatientDAO patientDAO = new PatientDAO(); // initialize once

    public static void main(String[] args) {
        try {
            // 1. Connection to the ActiveMQ
            ConnectionFactory factory = new ActiveMQConnectionFactory(HospitalMQ.BROKER_URL);

            // NB: we must trust the 'hospital' package to allow sending objects
            ((ActiveMQConnectionFactory) factory).setTrustAllPackages(true);

            Connection connection = factory.createConnection();
            connection.start();

            // 2. Create a session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 3. Create Destination (the same Topic used by Triage)
            Topic destination = session.createTopic(HospitalMQ.TRIAGE_TOPIC);

            // 4. Create a consumer
            MessageConsumer consumer = session.createConsumer(destination);

            System.out.println("Resource Service is waiting for patients...");

            // 5. Set up a Listener (This code runs every time a message arrives)
            consumer.setMessageListener(message -> {
                if (message instanceof ObjectMessage) {
                    try {
                        TriageEvent event = (TriageEvent) ((ObjectMessage) message).getObject();

                        // calculate where they should go
                        String targetWard = ResourceService.calculateAssignment(event);

                        // Capacity check
                        int currentOccupancy = patientDAO.getWardCount(targetWard);
                        int limit = ResourceService.getWardLimit(targetWard);

                        if (currentOccupancy >= limit) {
                            // hospital full - transfer logic
                            String hospitalX = "Chris Hani Baragwanath";
                            patientDAO.savePatient(event, targetWard, "TRANSFERRED", hospitalX);
                            System.out.println("ALERT: " + targetWard + " FULL. Transferring " + event.getPatientName() + " to " + hospitalX) ;
                        } else {
                            // space available - admit
                            patientDAO.savePatient(event, targetWard, "ADMITTED", "NONE");
                            System.out.println("SUCCESS: " + event.getPatientName() + " admitted to " + targetWard);
                        }

                        // Save to database (the real system)
//                        String zone = event.getTriageLevel().equals("RED") ? "ER-Zone-A" : "General-B";
//                        String bedNumber = zone + "-" + (int)(Math.random() * 100);
//
//                        patientDAO.savePatient(event, bedNumber);
//                        System.out.println("Persistent Record: : " + event.getPatientName() + "assigned to " + bedNumber);
                        //processPatient(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Resource Service Error: " + e.getMessage());
        }
    }

    private static void processPatient(TriageEvent event) {
        System.out.println("\n New Patient Alert");
        System.out.println("Patient: " + event.getPatientName());
        System.out.println("Priority: " + event.getTriageLevel());

        // Simple logic to "assign" resources based on priority
        if (event.getTriageLevel().equalsIgnoreCase("RED")) {
            System.out.println("Action: Assigning Emergency Room and Ventilator immediately");
        } else {
            System.out.println("Action: Adding to regular ward waiting list");
        }

    }

    // instead of just picking a random bed, it will now filter by ward
    // determine ward and bed
    // calls this to see where the patient should go
    // then checks DB if (patientDAO.getWordCount(ICU) >= 5), if full it changes the result to "TRANSFER"
    private static String calculateAssignment(TriageEvent event) {

        // 1. LEVEL RED: Emergency Overrides everything (Always ICU)
        if ("RED".equalsIgnoreCase(event.getTriageLevel())) {
            return "ICU-Trauma-Bay (Bed" + (int)(Math.random() * 5 + 1) + ")";
        }

        // PSYCHIATRIC / MENTAL HEALTH Logic
        if ("Psychiatric".equalsIgnoreCase(event.getTriageLevel())) {
            return "Behavioral-Health-Unit (Room BHU-" + (int)(Math.random() * 20 + 1) + ")";
        }

        // 2. MATERNAL CARE: Post-Nata and Maternity
        if ("Post-Natal".equalsIgnoreCase(event.getPatientCategory())) {
            return "Post-Natal-Recovery (Room PN-" + (int)(Math.random() * 10 + 1) + ")";
        }
        if ("Maternity".equalsIgnoreCase(event.getPatientCategory())) {
            return "Maternity-Labor-Suite (Room MAT-" + (int)(Math.random() * 10 + 1) + ")";
        }

        // 3.  PEDIATRICS: Anyone under 18
        if (event.getAge() < 18) {
            return "Pediatrics-Wing (Bed PED-" + (int)(Math.random() * 20 + 1) + ")";
        }

        // 4. ADULT GENERAL: Split by Gender and patientCategory
        if ("Male".equalsIgnoreCase(event.getGender())) {
            return "Male-Surgical-Ward (Bed MS-" + (int)(Math.random() * 30 + 1) + ")";
        } else {
            // Includes Gynaecological category for adult females
            return "Female-General-Ward (Bed FG-" + (int)(Math.random() * 30 + 1) + ")";
        }
    }

//    public void onMessage(Message message) {
//        // Receive the clean Data Object
//        TriageEvent event = (TriageEvent) ((ObjectMessage) message).getObject();
//
//        // Determine the IDEAL ward
//        String targetWard = determineIdealWard(event);
//
//        // CAPACITY cHECK: Check if the ward is full
//        int currentOccupancy = patientDAO.getWardCount(targetWard);
//        int maxCapacity getWardLimit(targetWard);
//
//        if (currentOccupancy >= maxCapacity) {
//            // trigger transfer logic
//            String destination = "Chris Hani Baragwanath Hospital"; // EXAMPLE
//            patientDAO.savePatient(event, targetWard, destination, "CAPACITY FULL");
//            System.out.println("ALERT: Ward " + targetWard + " is FULL. Transferring " + event.getPatientName());
//        } else {
//            // admit only
//            String bedNumber = targetWard.substring(0, 3) + "-" + (currentOccupancy + 1);
//            patientDAO.savePatient(event, targetWard, bedNumber);
//        }
//    }

    private static int getWardLimit(String ward) {
        if (ward.contains("ICU")) return 5;
        if (ward.contains("Psychiatric")) return 8;
        return 20; // default for general wards
    }




}

/*
The Clinical Hierarchy of Placement
How it works in a real hospital:
1. Red Rule: Life over Speciality
2. Stabilization Rule: patient stays in the ICU until their vitals are stable
3. Step-down rule: once the patient is no longer at immediate risk of death
 */
