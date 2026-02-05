// 1. Admit Patient Logic
async function admitPatient() {
    const nameField = document.getElementById('pName');
    const ageField = document.getElementById('pAge');

    const data = {
        patientName: nameField.value,
        triageLevel: document.getElementById('pLevel').value,
        patientCategory: document.getElementById('pCategory').value,
        gender: document.getElementById('pGender').value,
        age: parseInt(ageField.value),
        handledBy: "Admin"
    };

    // simple validation check before sending
    if (!data.patientName || isNaN(data.age)) {
        alert("Please enter both a name and a valid age.");
        return;
    }

    try {
        const response = await fetch('/admit-patient', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if(response.ok) {
            // clear all inputs for the next patient
            nameField.value = '';
            ageField.value = '';

            // reset dropdowns to their first option
            document.getElementById('pLevel').selectedIndex = 0;
            document.getElementById('pCategory').selectedIndex = 0;
            document.getElementById('pGender').value = 0;

            loadHistory(); // Refresh table immediately
            updateStats();
            console.log("Patient admitted and dashboard synced.")
        } else {
            alert("Admission failed. Server returned: " + response.statusText;)
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


            // for row color based on triage level
            const priorityClass = p.level ? `priority-${p.level.toLowerCase()}` : '';


            return `
             <tr class="${priorityClass}">
                  <td>${p.name}</td>
                  <td><strong>${p.ward}</strong></td>
                  <td><span class="status-tag status-${currentStatus.toLowerCase()}">${currentStatus}</span></td>
                  <td>${p.time || 'N/A'}</td>
                  <td>
                      ${currentStatus.toUpperCase() === 'ADMITTED' ? `
                         <button class="btn-discharge" onclick="dischargePatient(${currentId})">Discharge</button>
                         <button class="btn-transfer" onclick="transferPatient(${currentId})">Transfer</button>
                         ` : `<small>Done: ${p.destination || 'N/A'}</small>`}
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
    const reason = prompt("Enter discharge reason (e.g., Recovered, Home Care, Deceased): ")

    if (reason) {
        await fetch(`/api/patient/${id}/discharge?reason=${encodeURIComponent(reason)}`, {
             method: 'POST'
        });
        loadHistory(); // Refresh table
        updateStats();
    }
}

// Action: Transfer
async function transferPatient(id) {
    const hospital = prompt("Enter destination hospital (e.g., Chris Hani Baragwanath):");
    if (hospital) {
        await fetch(`/api/patient/${id}/transfer?to=${hospital}`, { method: 'POST' });
        loadHistory(); // Refresh table
        updateStats();
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

function filterHistory() {
    const input = document.getElementById('historySearch');
    const filter = input.value.toLowerCase();
    const table = document.querySelector("#historyTable"); // Make sure your table has this ID
    const tr = table.getElementsByTagName('tr');

    // Loop through all rows (skipping the header at index 0)
    for (let i = 1; i < tr.length; i++) {
        const nameColumn = tr[i].getElementsByTagName('td')[0]; // Name is the first column
        if (nameColumn) {
            const txtValue = nameColumn.textContent || nameColumn.innerText;
            if (txtValue.toLowerCase().indexOf(filter) > -1) {
                tr[i].style.display = "";
            } else {
                tr[i].style.display = "none";
            }
        }
    }
}

// 3. WebSocket Live Updates
const protocol = location.protocol === "https:" ? "wss://" : "ws://";
const ws = new WebSocket(protocol + location.host + "/live-triage");

ws.onmessage = (msg) => {
    const data = JSON.parse(msg.data);
    const feed = document.getElementById('feed');

    const div = document.createElement('div');

    const priority = data.level ? data.level.toLowerCase() : 'green' ;

    div.className = `patient-card ${priority} animate-pulse`;
    div.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center;">
            <strong>${data.name}</strong>
            <span class="status-tag priority-${priority}">${data.level}</span>
        </div>
        <div style="margin-top: 5px">
            <small>Category: <strong>${data.category}</strong></small><br>
            <small>Staff: ${data.handledBy || 'System'}</small>
        </div>
    `;

    feed.prepend(div);

    loadHistory();
    updateStats();

    // auto-remove alert form feed after 1 hour to prevent memory bloat
    setTimeout(() => div.remove(), 3600000);
};

// Auto-load history on page load
//window.onload = loadHistory;