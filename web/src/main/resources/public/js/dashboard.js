// 1. Admit Patient Logic
async function admitPatient() {
    const data = {
        patientName: document.getElementById('pName').value,
        triageLevel: document.getElementById('pLevel').value,
        patientCategory: document.getElementById('pCategory').value,
        gender: document.getElementById('pGender').value,
        age: parseInt(document.getElementById('pAge').value),
        handledBy: "Admin"
    };

    try {
        const response = await fetch('/admit-patient', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if(response.ok) {
            document.getElementById('pName').value = '';
            loadHistory(); // Refresh table immediately
        }
    } catch (error) {
        console.error("Failed to admit patient:", error);
    }
}

// 2. Load History Table
async function loadHistory() {
    try {
        const response = await fetch('/api/history');
        const patients = await response.json();
        const body = document.getElementById('historyBody');

        body.innerHTML = patients.map(p => {
            const currentStatus = p.status || 'ADMITTED';
            const currentId = p.id;
            // get time form the JSON or show "N/A" if empty
            const timeAdmitted = p.time || 'Pending...'

            return `
             <tr>
                  <td>${p.name}</td>
                  <td><strong>${p.ward}</strong></td>
                  <td><span class="status-tag status-${currentStatus.toLowerCase()}">${currentStatus}</span></td>
                  <td>${p.time || 'N/A'}</td> <td>
                      ${currentStatus.toUpperCase() === 'ADMITTED' ? `
                         <button class="btn-discharge" onclick="dischargePatient(${currentId})">Discharge</button>
                         <button class="btn-transfer" onclick="transferPatient(${currentId})">Transfer</button>
                         ` : `<small>Done: ${p.destination || ''}</small>`}
                </td>
             </tr>
            `;
        }).join('');
    } catch (error) {
        console.error("Error loading history:", error)
    }



}

// Action: Discharge
async function dischargePatient(id) {
    if (confirm("Are you sure you want to discharge this patient?")) {
        await fetch(`/api/patient/${id}/discharge`, { method: 'POST' });
        loadHistory(); // Refresh table
    }
}

// Action: Transfer
async function transferPatient(id) {
    const hospital = prompt("Enter destination hospital (e.g., Chris Hani Baragwanath):");
    if (hospital) {
        await fetch(`/api/patient/${id}/transfer?to=${hospital}`, { method: 'POST' });
        loadHistory(); // Refresh table
    }
}

async function updateStats() {
    try {
        const response = await fetch('/api/stats');
        const stats = await response.json();

        const wards = [
            { id: 'ICU', limit: 5 },
            { id: 'Maternity', limit: 10 },
            { id: 'Psychiatric', limit: 8 },
            { id: 'General', limit: 20 },
            { id: 'Pediatric', limit: 30 }
        ];

        wards.forEach(ward => {
            const matchingKey = Object.keys(stats).find(key => key.includes(ward.id));
            const count = matchingKey ? stats[matchingKey] : 0;
            const percentage = (count / ward.limit) * 100;

            // Notice the spelling: getElementById
            const textElem = document.getElementById(`stat-${ward.id}`);
            const barElem = document.getElementById(`bar-${ward.id}`);

            if (textElem && barElem) {
                textElem.innerText = `${count} / ${ward.limit}`;
                barElem.style.width = `${Math.min(percentage, 100)}%`;
            } else {
                console.warn(`Missing HTML elements for ward: ${ward.id}`);
            }
        });
    } catch (error) {
        console.error("Stats update failed:", error);
    }
}

// Call this inside window.onload and after every Discharge/Admit
window.onload = () => {
    loadHistory();
    updateStats();
    setInterval(updateStats, 5000)
};

// 3. WebSocket Live Updates
const protocol = location.protocol === "https:" ? "wss://" : "ws://";
const ws = new WebSocket(protocol + location.host + "/live-triage");

ws.onmessage = (msg) => {
    const data = JSON.parse(msg.data);
    const feed = document.getElementById('feed');
    const div = document.createElement('div');

    div.className = `patient-card ${data.level}`;
    div.innerHTML = `
        <strong>${data.name}</strong> admitted to <strong>${data.category}</strong><br>
        <small>Priority: ${data.level} | Staff: ${data.handledBy || 'Unknown'}</small>
    `;

    feed.prepend(div);
};

// Auto-load history on page load
//window.onload = loadHistory;