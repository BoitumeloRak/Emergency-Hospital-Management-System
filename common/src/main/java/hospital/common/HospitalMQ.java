package hospital.common;

/*
 * Store channels names here
 * The "Post Office. It holds the message and delivers it to anyone interested
 */
public interface HospitalMQ {
    String BROKER_URL = "tcp://172.18.48.1:61616"; // Standard ActiveMQ connection URL
    String TRIAGE_TOPIC = "hospital.triage"; // The topic name all services will use to talk about triage

}
