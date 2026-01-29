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

        body.innerHTML = patients.map(p => `
            <tr>
                <td>${p.name}</td>
                <td><strong>${p.ward}</strong></td>
                <td><span class="status-tag status-${p.status.toLowerCase()}">${p.status}</span></td>
                <td>
                    ${p.status === 'ADMITTED' ? `
                        <button class="btn-discharge" onclick="dischargePatient(${p.id})">Discharge</button>
                        <button class="btn-transfer" onclick="transferPatient(${p.id})">Transfer</button>
                        ` : `<small>Done: ${p.destination || ''}</small>`}
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error("Error loading history:", error);
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
    const response = await fetch('/api/stats');
    const stats = await response.json();

    const wards = [
        { id: 'ICU', limit: 5, name: 'ICU' },
        { id: 'Maternity', limit: 10, name: 'Maternity-Ward' },
        { id: 'Psychiatric', limit: 8, name: 'Psychiatric-Ward' }
    ];

    wards.forEach(ward => {
        // Find the count in the data (handle cases where ward name varies)
        const count = stats[ward.name] || 0;
        const percentage = (count / ward.limit) * 100;

        document.getElementById(`stat-${ward.id}`).innerText = `${count} / ${ward.limit}`;
        document.getElementById(`bar-${ward.id}`).style.width = `${Math.min(percentage, 100)}%`;

        // Change color to red if ward is full
        if (percentage >= 100) {
            document.getElementById(`bar-${ward.id}`).style.background = "#e74c3c";
        } else {
            document.getElementById(`bar-${ward.id}`).style.background = "#2ecc71";
        }
    });
}

// Call this inside window.onload and after every Discharge/Admit
window.onload = () => {
    loadHistory();
    updateStats();
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
window.onload = loadHistory;