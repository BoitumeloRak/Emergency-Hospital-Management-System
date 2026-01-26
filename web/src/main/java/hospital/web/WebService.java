package hospital.web;

import hospital.common.HospitalMQ;
import hospital.common.TriageEvent;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebService {
    // Keep track of all open browser windows
    private static final Set<WsContext> client = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void main(String[] args) throws JMSException {
        // 1. Start Javalin for the Web Dashboard on Port 7002
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public"); // put html here
        }).start(7002);

        // 2. Setup WebSocket endpoint
        app.ws("/live-triage", ws -> {
            ws.onConnect(ctx -> client.add(ctx));
            ws.onClose(ctx -> client.remove(ctx));
        });

        // 3. Connect to ActiveMQ to CONSUME events
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(HospitalMQ.BROKER_URL);
        factory.setTrustAllPackages(true);

        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(HospitalMQ.TRIAGE_TOPIC);
        MessageConsumer consumer = session.createConsumer(topic);

        // 4. WHen a message arrives, send it to the Browsers
        consumer.setMessageListener(message -> {
            try {
                if (message instanceof ObjectMessage) {
                    TriageEvent event = (TriageEvent) ((ObjectMessage) message).getObject();
                    String jsonUpdate = String.format("{\"name\": \"%s\", \"level\": \"%s\"}",
                            event.getPatientName(), event.getTriageLevel());

                    // Push to all connected browsers
                    client.forEach(client -> client.send(jsonUpdate));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println("Web Dashboard Service running...");


    }
}
