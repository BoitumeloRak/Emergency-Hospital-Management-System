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

    // Simple validation check before sending
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

        if (response.ok) {
            // Clear all inputs for the next patient
            nameField.value = '';
            ageField.value = '';

            // Reset dropdowns to their first option
            document.getElementById('pLevel').selectedIndex = 0;
            document.getElementById('pCategory').selectedIndex = 0;
            document.getElementById('pGender').selectedIndex = 0;

            loadHistory(); // Refresh table immediately
            updateStats();
            console.log("Patient admitted and dashboard synced.");
        } else {
            alert("Admission failed. Server returned: " + response.statusText);
        }
    } catch (error) {
        console.error("Failed to admit patient:", error);
        alert("Failed to connect to the server. Please try again.");
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
            const timeAdmitted = p.time || 'Pending...';
            const priorityClass = p.level ? `priority-${p.level.toLowerCase()}` : '';

            return `
                <tr class="${priorityClass}">
                    <td>${p.name}</td>
                    <td><strong>${p.ward}</strong></td>
                    <td><span class="status-tag status-${currentStatus.toLowerCase()}">${currentStatus}</span></td>
                    <td>${timeAdmitted}</td>
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
        console.error("Error loading history:", error);
    }
}

// 3. Discharge Patient
async function dischargePatient(id) {
    const reason = prompt("Enter discharge reason (e.g., Recovered, Home Care, Deceased):");

    if (reason) {
        try {
            await fetch(`/api/patient/${id}/discharge?reason=${encodeURIComponent(reason)}`, {
                method: 'POST'
            });
            loadHistory();
            updateStats();
        } catch (error) {
            console.error("Error discharging patient:", error);
            alert("Failed to discharge patient. Please try again.");
        }
    }
}

// 4. Transfer Patient
async function transferPatient(id) {
    const hospital = prompt("Enter destination hospital (e.g., Chris Hani Baragwanath):");
    if (hospital) {
        try {
            await fetch(`/api/patient/${id}/transfer?to=${encodeURIComponent(hospital)}`, {
                method: 'POST'
            });
            loadHistory();
            updateStats();
        } catch (error) {
            console.error("Error transferring patient:", error);
            alert("Failed to transfer patient. Please try again.");
        }
    }
}

// 5. Update Stats
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
            const percentage = Math.min((count / ward.limit) * 100, 100);

            const textElem = document.getElementById(`stat-${ward.id}`);
            const barElem = document.getElementById(`bar-${ward.id}`);

            if (textElem && barElem) {
                textElem.innerText = `${count} / ${ward.limit}`;
                barElem.style.width = `${percentage}%`;
            }
        });
    } catch (error) {
        console.error("Stats update failed:", error);
    }
}

// 6. Filter History
function filterHistory() {
    const input = document.getElementById('historySearch');
    const filter = input.value.toLowerCase();
    const table = document.getElementById('historyTable');
    const tr = table.getElementsByTagName('tr');

    for (let i = 1; i < tr.length; i++) {
        const nameColumn = tr[i].getElementsByTagName('td')[0];
        if (nameColumn) {
            const txtValue = nameColumn.textContent || nameColumn.innerText;
            tr[i].style.display = txtValue.toLowerCase().includes(filter) ? "" : "none";
        }
    }
}

// 7. WebSocket Initialization
function initializeWebSocket() {
    const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
    const ws = new WebSocket(protocol + window.location.host + "/live-triage");

    ws.onopen = () => {
        console.log("WebSocket connection established");
    };

    ws.onmessage = (msg) => {
        try {
            const data = JSON.parse(msg.data);
            const feed = document.getElementById('feed');

            if (feed) {
                const div = document.createElement('div');
                const priority = (data.level || '').toLowerCase() || 'green';

                div.className = `patient-card ${priority} animate-pulse`;
                div.innerHTML = `
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <strong>${data.name || 'Unknown'}</strong>
                        <span class="status-tag priority-${priority}">${data.level || 'UNKNOWN'}</span>
                    </div>
                    <div style="margin-top: 5px">
                        <small>Category: <strong>${data.category || 'N/A'}</strong></small><br>
                        <small>Staff: ${data.handledBy || 'System'}</small>
                    </div>
                `;

                feed.prepend(div);
                loadHistory();
                updateStats();

                // Auto-remove alert from feed after 1 hour
                setTimeout(() => div.remove(), 3600000);
            }
        } catch (error) {
            console.error("Error processing WebSocket message:", error);
        }
    };

    ws.onerror = (error) => {
        console.error("WebSocket error:", error);
    };

    ws.onclose = () => {
        console.log("WebSocket connection closed. Attempting to reconnect...");
        setTimeout(initializeWebSocket, 5000); // Try to reconnect after 5 seconds
    };
}

// Initialize everything when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', function() {
    loadHistory();
    updateStats();
    initializeWebSocket();
    setInterval(updateStats, 5000);

    // Add event listener for the search input
    const searchInput = document.getElementById('historySearch');
    if (searchInput) {
        searchInput.addEventListener('input', filterHistory);
    }

    // Add event listener for the form submission
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            admitPatient();
        });
    }
});