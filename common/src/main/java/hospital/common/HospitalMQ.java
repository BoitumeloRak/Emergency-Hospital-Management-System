package hospital.common;

/*
 * Store channels names here
 */
public interface HospitalMQ {
    String BROKER_URL = "tcp://localhost:61616"; // Standard ActiveMQ connection URL
    String TRIAGE_TOPIC = "hospital.triage.updates"; // The topic name all services will use to talk about triage
}
