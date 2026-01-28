package hospital.triage;

import hospital.common.HospitalMQ;
import hospital.common.TriageEvent;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/*
 * This is the main application that will run on Port 7001
 * This service is the Producer, It acts as the "front door of the hospital
 * where the nurse enters the patient data.
 * It does 2 things:
 * 1. Provides a REST API using Javalin (so you can send data via a web request)
 * 2. Sends that dat to ActiveMQ as a message for everyone else to see.
 * Takes data from the nurse (REST API) and "produces" a message to ActiveMQ
 *
 * only job is to a POST OFFICE
 * It takes the "Envelope" (the patient data) and drops it in the "Mailbox" (ActiveMQ).
 */

public class TriageService {
    public static void main(String[] args) {

        // Start Javalin Web Server
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost();
                });
            });
        }).start(7001);

        // This is the endpoint the nurse uses to admit a patient
        app.post("/admit", ctx -> {
            // 1. receive JSON from the request and turn it into a TriageEvent object
            TriageEvent event = ctx.bodyAsClass(TriageEvent.class);

            // 2. Publish that event to ActiveMQ
            boolean sent = publishToMQ(event);

            if (sent) {
                ctx.status(201).result("Patient " + event.getPatientName() + " admitted and broadcasted!");
            } else {
                ctx.status(500).result("System error: ActiveMQ is likely offline.");
            }
        });
        System.out.println("Triage service is running on http://localhost:7001");
    }

    private static boolean publishToMQ(TriageEvent event) {
        try {
            // 1. Create a connection to the ActiveMQ broker
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(HospitalMQ.BROKER_URL);
            factory.setTrustAllPackages(true);

            Connection connection = factory.createConnection();
            connection.start();

            // 2. Create a session to send messages
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 3. Define the destinations (The Topic we defined in Common)
            Destination destination = session.createTopic(HospitalMQ.TRIAGE_TOPIC);

            // 4. Create a producer and the actual Message
            MessageProducer producer = session.createProducer(destination);
            ObjectMessage message = session.createObjectMessage(event);

            // 5. Send it
            producer.send(message);

            System.out.println("Broadcasted: " + event.getPatientName() + " (Level: " + event.getTriageLevel() + ")");

            // 6. Clean up
            connection.close();
            return true;
        } catch (Exception e) {
//            System.err.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }
}
