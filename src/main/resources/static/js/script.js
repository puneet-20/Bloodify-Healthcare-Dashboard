let isDarkMode = false;
let currentImageBase64 = null;
let probabilityChartObj = null;

// Initialization
document.addEventListener("DOMContentLoaded", () => {
    loadAllDonors(); // Which also gets metrics
});

function toggleDarkMode() {
    isDarkMode = !isDarkMode;
    document.documentElement.setAttribute('data-theme', isDarkMode ? 'dark' : 'light');
    document.getElementById("theme-icon").className = isDarkMode ? "fa-solid fa-sun" : "fa-solid fa-moon";
    if(probabilityChartObj) {
        probabilityChartObj.options.plugins.legend.labels.color = isDarkMode ? '#f8fafc' : '#1e293b';
        probabilityChartObj.update();
    }
}

function openTab(evt, tabName) {
    const tabcontents = document.getElementsByClassName("tab-content");
    for (let i = 0; i < tabcontents.length; i++) tabcontents[i].style.display = "none";
    
    const tablinks = document.getElementsByClassName("tab-btn");
    for (let i = 0; i < tablinks.length; i++) tablinks[i].classList.remove("active");
    
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.classList.add("active");
}

// Eligibility Checker
function checkEligibility() {
    const dateStr = document.getElementById("lastDonation").value;
    const health = document.getElementById("healthCondition").value;
    const badge = document.getElementById("eligibilityBadge");
    const saveBtn = document.getElementById("saveDonorBtn");
    
    if(!dateStr || !health) return;

    const last = new Date(dateStr);
    const today = new Date();
    const diffTime = Math.abs(today - last);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
    
    let isEligible = true;
    let reason = "";
    
    if(health !== "Good") {
        isEligible = false;
        reason = "Health unsuitable";
    } else if(diffDays < 56) {
        isEligible = false;
        reason = `Wait ${56 - diffDays} more days`;
    }
    
    if(isEligible) {
        badge.className = "eligibility-status eligible";
        badge.innerHTML = '<i class="fa-solid fa-check"></i> Eligible to Donate';
        saveBtn.disabled = false;
    } else {
        badge.className = "eligibility-status not-eligible";
        badge.innerHTML = `<i class="fa-solid fa-xmark"></i> Not Eligible (${reason})`;
        saveBtn.disabled = true;
    }
}

async function captureFingerprint() {
    try {
        const response = await fetch('https://localhost:8443/SGIFPCapture', { method: 'POST' });
        const data = await response.json();
        if(data && data.ErrorCode === 0) {
            currentImageBase64 = data.BMPBase64;
            displayImage("data:image/bmp;base64," + currentImageBase64);
            predictBloodGroup(currentImageBase64);
        } else {
            alert("Device error code: " + (data ? data.ErrorCode : "Unknown"));
        }
    } catch (err) {
        alert("SecuGen WebAPI not detected. Using file upload fallback for demo.");
    }
}

function handleFileUpload(event) {
    const file = event.target.files[0];
    if(file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const result = e.target.result;
            displayImage(result);
            const base64Data = result.split(',')[1];
            currentImageBase64 = base64Data;
            predictBloodGroup(base64Data);
        };
        reader.readAsDataURL(file);
    }
}

function displayImage(src) {
    document.getElementById("fingerprintImage").src = src;
    document.getElementById("imageContainer").style.display = "block";
}

async function predictBloodGroup(base64Image) {
    try {
        document.getElementById("predictionPlaceholder").style.display = "none";
        
        const response = await fetch('/api/predict', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ image: base64Image })
        });
        const data = await response.json();
        
        document.getElementById("predictionBox").style.display = "block";
        document.getElementById("predictedGroupValue").innerText = data.bloodGroup;
        
        const badge = document.getElementById("confidenceLevelBadge");
        badge.innerText = `AI Conf: ${data.confidenceLevel} (${data.confidenceScore}%)`;
        badge.style.background = data.confidenceLevel === "High" ? "#dcfce7" : (data.confidenceLevel==="Medium" ? "#fef08a" : "#fecaca");
        badge.style.color = data.confidenceLevel === "High" ? "#166534" : (data.confidenceLevel==="Medium" ? "#854d0e" : "#991b1b");
        
        document.getElementById("donorBloodGroup").value = data.bloodGroup;
        
        renderChart(data.distribution);
    } catch (err) {
        console.error("Prediction failed:", err);
    }
}

function renderChart(distMap) {
    const ctx = document.getElementById('probabilityChart').getContext('2d');
    const labels = Object.keys(distMap);
    const dataVals = Object.values(distMap);
    
    if(probabilityChartObj) probabilityChartObj.destroy();
    
    probabilityChartObj = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Probability %',
                data: dataVals,
                backgroundColor: 'rgba(211, 47, 47, 0.7)',
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: { beginAtZero: true, max: 100 }
            }
        }
    });
}

async function saveDonor(event) {
    event.preventDefault();
    const donor = {
        name: document.getElementById("donorName").value,
        age: parseInt(document.getElementById("donorAge").value),
        city: document.getElementById("donorCity").value,
        contact: document.getElementById("donorContact").value,
        bloodGroup: document.getElementById("donorBloodGroup").value
    };
    
    try {
        const response = await fetch('/api/donor', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(donor)
        });
        const msgBox = document.getElementById("formMessage");
        if(response.ok) {
            msgBox.innerHTML = "<p style='color: var(--success-color); margin-top:10px;'><i class='fa-solid fa-circle-check'></i> Donor registered successfully!</p>";
            event.target.reset();
            document.getElementById("eligibilityBadge").className = "eligibility-status";
            document.getElementById("eligibilityBadge").innerText = "Awaiting Input";
            document.getElementById("saveDonorBtn").disabled = true;
            loadAllDonors(); // refresh stats
        } else {
            const err = await response.json();
            msgBox.innerHTML = "<p style='color: var(--danger-color); margin-top:10px;'><i class='fa-solid fa-circle-xmark'></i> " + err.error + "</p>";
        }
    } catch (err) {
        console.error("Save failed:", err);
    }
}

async function loadAllDonors() {
    try {
        const response = await fetch('/api/donors');
        const list = await response.json();
        
        // Update top metrics
        document.getElementById("totalDonorsCount").innerText = list.length;
        
        // Render table
        const tbody = document.querySelector("#allDonorsTable tbody");
        tbody.innerHTML = "";
        list.forEach(d => {
            tbody.innerHTML += `
                <tr>
                    <td>#${d.id}</td>
                    <td>${d.name}</td>
                    <td>${d.age}</td>
                    <td><span class="badge ${d.bloodGroup.startsWith('A')?'badge-a':d.bloodGroup.startsWith('B')?'badge-b':'badge-o'}">${d.bloodGroup}</span></td>
                    <td>${d.city || 'N/A'}</td>
                    <td>${d.contact}</td>
                    <td style="font-size:0.85em; color:var(--text-secondary);">${d.timestamp ? d.timestamp.substring(0,10):''}</td>
                </tr>
            `;
        });
        
        // Empty Search Results initially
        document.getElementById("compatibleDonorsContainer").innerHTML = "<p class='placeholder-text'>Select filters and search to find compatible donors.</p>";
    } catch (err) {
        console.error(err);
    }
}

async function searchCompatibleDonors() {
    const selectedBg = document.getElementById("searchBloodGroup").value;
    const city = document.getElementById("searchCity").value;
    const isEmerg = document.getElementById("emergencyToggle").checked;
    
    let url = `/api/donors/compatible?receiverBg=${encodeURIComponent(selectedBg)}&emergency=${isEmerg}`;
    if(city) url += `&city=${encodeURIComponent(city)}`;
    
    try {
        const response = await fetch(url);
        const list = await response.json();
        
        const container = document.getElementById("compatibleDonorsContainer");
        container.innerHTML = list.length === 0 ? "<div class='card'><p style='text-align:center;'>No compatible donors found.</p></div>" : "";
        
        list.forEach(d => {
            const isUniversal = isEmerg && d.bloodGroup === 'O-';
            const html = `
                <details class="stExpander ${isUniversal ? 'emergency-card' : ''}">
                    <summary>
                        <span><i class="fa-solid fa-droplet"></i> ${d.bloodGroup} | ${d.name}</span>
                        ${isUniversal ? '<span class="badge" style="background:var(--danger-color);color:white;">Universal Urgent Matches</span>' : ''}
                    </summary>
                    <div class="details-content">
                        <div class="card-badges">
                            <span class="badge badge-loc"><i class="fa-solid fa-location-dot"></i> ${d.city || 'Any'}</span>
                            <span class="badge badge-loc"><i class="fa-regular fa-clock"></i> Last Active: ${d.timestamp ? d.timestamp.substring(0,10):'Unknown'}</span>
                        </div>
                        <p><strong>Contact:</strong> ${d.contact} | <strong>Age:</strong> ${d.age}</p>
                        
                        <div class="contact-actions">
                            <button class="stButton btn-sm action-btn" onclick="downloadReport(${d.id})"><i class="fa-solid fa-file-pdf"></i> Download FPDF</button>
                            <a href="tel:${d.contact}" class="stButton btn-sm"><i class="fa-solid fa-phone"></i> Call</a>
                            <a href="mailto:dummy@example.com" class="stButton btn-sm"><i class="fa-solid fa-envelope"></i> Email</a>
                            <a href="https://wa.me/${d.contact.replace(/\D/g, '')}" target="_blank" class="stButton btn-sm whatsapp-btn"><i class="fa-brands fa-whatsapp"></i> WhatsApp</a>
                        </div>
                    </div>
                </details>
            `;
            container.innerHTML += html;
        });
    } catch (err) {
        console.error(err);
    }
}

function downloadReport(id) {
    window.location.href = `/api/donor/${id}/pdf`;
}
