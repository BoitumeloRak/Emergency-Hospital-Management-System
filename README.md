# 🏥 Hospital Emergency Coordination System (HECS)

> **A real-time, distributed microservices ecosystem designed to synchronize hospital departments during critical patient intake.**

---

## 🌟 The Problem & Solution
In busy South African hospitals, communication delays between **Triage** (the front door) and the **ICU/Wards** (the resources) can be life-threatening. 

**HECS** bridges this gap by using a **Message Broker** to broadcast patient status instantly. When a nurse admits a patient, the information is "pushed" to dashboards across the hospital—no phone calls required.

---

## 🏗 System Architecture
This project is built as a **Maven Multi-Module** system to ensure services are **Loosely Coupled**.

### 🔹 Service Breakdown
* **`common/`**: The **Data Contract**. Contains shared objects like `TriageEvent`.
* **`triage-service/`**: The **Producer**. A REST-based service (**Port 7001**) for patient intake.
* **`resource-service/`**: The **Consumer**. A logic engine (**Port 7002**) tracking bed availability.
* **`web-dashboard/`**: The **Live UI**. A web-based "Mission Control" (**Port 7070**) for real-time alerts.



---

## 📡 Messaging Logic (ActiveMQ)
We use a **Topic (Pub-Sub)** model for the `TRIAGE_TOPIC`. This follows the **South African Triage Scale (SATS)**.

| Triage Level | Priority | Action Taken by System |
| :--- | :---: | :--- |
| **🔴 RED** | **High** | Immediate dashboard flash; reserves ICU bed. |
| **🟠 ORANGE** | **Urgent** | Flashing alert; notifies senior doctor. |
| **🟡 YELLOW** | **Routine** | Added to standard waiting list. |
| **🟢 GREEN** | **Low** | Logged for record-keeping. |

---

## 🚀 Installation & Running

### ⚙️ Prerequisites
1.  **Java 17+**
2.  **ActiveMQ Classic** (Running at `tcp://localhost:61616`)
3.  **Maven 3.8+**

### 💻 Quick Start (Build Script)
We use a automation script to build and launch all services simultaneously:
```bash
chmod +x run_hospital.sh
./run_hospital.sh
