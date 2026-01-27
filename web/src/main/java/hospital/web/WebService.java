package hospital.web;

import hospital.common.HospitalMQ;
import hospital.common.TriageEvent;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebService {
    // Keep track of all open browser windows
    private static final Set<WsContext> client = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void main(String[] args) throws JMSException {
        // 3. Connect to ActiveMQ to CONSUME events
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(HospitalMQ.BROKER_URL);
        factory.setTrustAllPackages(true);

        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(HospitalMQ.TRIAGE_TOPIC);
        MessageConsumer consumer = session.createConsumer(topic);

        // 1. Start Javalin for the Web Dashboard on Port 7002
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public"); // put html here
        }).start(7002);

        // Professional Login Route
        app.post("/login", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String username = body.get("username");
            String password = body.get("password");

            if ("admin".equals(password)) {
                ctx.sessionAttribute("currentUser", username);
                ctx.json(Map.of("status", "success", "user", username));
            } else {
                ctx.status(401).result("Unauthorized: Wrong Password");
            }
        });


        // Protected Admission Route
        app.post("/admit-patient", ctx -> {
            TriageEvent event = ctx.bodyAsClass(TriageEvent.class);

            // Security layer: Log the actual network source
            String ipAddress = ctx.ip();
            String userAgent = ctx.header("User-Agent");

            // Attach this to the event (requires adding fields to TriageEvent.java)
            // event.setSourceIp(ipAddress);

            System.out.println("Alert: Patient admitted from IP: " + ipAddress + " by " + event.getHandledBy());

            String staff = ctx.sessionAttribute("currentUser");
            if (staff == null) {
                ctx.status(403).result("You must be logged un to admit patients");
                return;
            }
           // TriageEvent event = ctx.bodyAsClass(TriageEvent.class);
            //event.setHandleBy(staff); // Log WHO did the work

            // Send to MQ immediately
            MessageProducer producer = session.createProducer(topic);
            ObjectMessage msg = session.createObjectMessage(event);
            producer.send(msg);
            producer.close();

            ctx.status(201).result("Success.");

        });

        // 2. Setup WebSocket  (for live updates)
        app.ws("/live-triage", ws -> {
            ws.onConnect(ctx -> client.add(ctx));
            ws.onClose(ctx -> client.remove(ctx));
        });

        // 4. When a message arrives, send it to the Browsers
//        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(message -> {
            try {
                if (message instanceof ObjectMessage) {
                    TriageEvent event = (TriageEvent) ((ObjectMessage) message).getObject();
                    String json = String.format("{\"name\": \"%s\", \"level\": \"%s\"}",
                            event.getPatientName(), event.getTriageLevel(), event.getHandledBy());

                    // Push to all connected browsers
                    client.forEach(client -> client.send(json));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println("Web Dashboard Service running...");


    }
}
