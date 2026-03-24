# 🏥 Hospital Emergency Coordination System (HECS)

> **A real-time, distributed microservices ecosystem designed to synchronize hospital departments during critical patient intake.**

---

## The Problem & Solution
In busy South African hospitals, communication delays between **Triage** (the front door) and the **ICU/Wards** (the resources) can be life-threatening. 

**HECS** bridges this gap by using an **Event-Driven Architecture**. When a nurse admits a patient, the information is pushed via a Message Broker to dashboards across the hospital-ensuring that ICU beds are prepared before the patient even leaves the triage room.

---

## System Architecture
This project is built as a **Maven Multi-Module** system to ensure services are **Loosely Coupled**.

### Service Breakdown
* **`common/`**: The **Data Contract**. Contains shared objects like `TriageEvent`.
* **`triage-service/`**: The **Producer**. A REST-based service (**Port 7001**) for patient intake.
* **`resource-service/`**: The **Consumer**. A logic engine (**Port 7002**) tracking bed availability & persistent storage (SQLite).
* **`web-dashboard/`**: The **Live UI**. A web-based "Mission Control" (**Port 7070**) for real-time alerts.



---

## 📡 Messaging Logic (ActiveMQ)
We use a **Topic (Pub-Sub)** model for the `TRIAGE_TOPIC`. This follows the **South African Triage Scale (SATS)**.

| Triage Level |   Priority    | Action Taken by System                   |
| :--- |:-------------:|:-----------------------------------------|
| **🔴 RED** | **Emergency** | Immediate flash; Auto-reservation logic. |
| **🟡 YELLOW** |  **Urgent**   | Highlighted alert; Priority queueing.    |
| **🟢 GREEN** |  **Routine**  | Standard logging; Capacity tracking.     |

---

### Tech Stack

- Backend: Java 17, Maven, SparkJava (REST)

- Messaging: Apache ActiveMQ (JMS)

- Database: SQLite (DAO Pattern)

- Frontend: HTML5/CSS3 (Flexbox), JavaScript (WebSockets)

### Future Roadmap: Phase 2
Based on our current build, the following enhancements are planned to make the system "Production-Ready":

1. **Enhanced Security:** Implementation of **JWT (JSON Web Tokens)** for secure staff login and **Data Encryption** for sensitive patient records (POPI Act compliance).

2. **Dashboard Analytics:** Integration of Chart.js to visualize patient flow trends and resource utilization over time.

3. **Scalability:** Containerizing the services using **Docker** and **Kubernetes** for easy deployment across multiple hospital sites.

4. **Reliability:** Adding **Audit Logging** to track every status change for clinical accountability.


### 💻 Manual Testing & Verification

[//]: # (We use a automation script to build and launch all services simultaneously:)
```bash
chmod +x run_hospital.sh
./run_hospital.sh

To actually test this:
1. Start ActiveMQ: Ensure the broker is running at tcp://localhost:61616
2. Build project: mvn clean install
3. Run Services: Launch ResourceService(Database & logic). Launch WebService.java (Dashboard & API)
4. Access UI: Open http://localhost:7070 in your browser
5. Send a Test: Use a tool like Postman or use this terminal command:
