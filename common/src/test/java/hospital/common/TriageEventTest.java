package hospital.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.*;

public class TriageEventTest {

    @Test
    void testEventCreation() {
        TriageEvent event = new TriageEvent("Boitumelo Rakgole", "RED");

        assertEquals("Boitumelo Rakgole", event.getPaitentName());
        assertEquals("RED", event.getTriageLevel());
        assertTrue(event.getTimestamp() > 0, "Timestamp should be initialized");
    }
}
