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
                        // Save to database (the real system)


                        String zone = event.getTriageLevel().equals("RED") ? "ER-Zone-A" : "General-B";
                        String bedNumber = zone + "-" + (int)(Math.random() * 100);

                        patientDAO.savePatient(event, bedNumber);
                        System.out.println("Persistent Record: : " + event.getPatientName() + "assigned to " + bedNumber);
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


}
