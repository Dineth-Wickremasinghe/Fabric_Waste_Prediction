const API_BASE = 'http://localhost:8080/api/waste';

let trendChartInstance         = null;
let fabricChartInstance        = null;
let styleChartInstance         = null;
let shiftChartInstance         = null;
let cuttingMethodChartInstance = null;
let riskDistChartInstance      = null;
let accuracyGapChartInstance   = null;

// ══════════════════════════════════════════
// AUTO LOAD ON PAGE OPEN
// ══════════════════════════════════════════
window.addEventListener('load', async () => {
    console.log('Dashboard loading...');
    await loadSummaryKPIs();
    await loadTrendChart();
    await loadShiftChart();
    await loadCuttingMethodChart();
    await loadRiskDistributionChart();
    await loadAccuracyGapChart();
    await loadFabricChart();
    await loadStyleChart();
    await loadLast5();
    await loadAccuracy();
    await loadHistory();
    await loadPredictionsDropdown();
    console.log('Dashboard loaded!');
});

// ══════════════════════════════════════════
// SUMMARY KPIs
// ══════════════════════════════════════════
async function loadSummaryKPIs() {
    try {
        const res  = await fetch(`${API_BASE}/summary`);
        const data = await res.json();
        document.getElementById('avgWaste').innerText     = data.avgWaste + ' %';
        document.getElementById('highRiskDays').innerText = data.highRiskDays + ' days';
        document.getElementById('bestFabric').innerText   = data.bestFabric;
        document.getElementById('worstShift').innerText   = data.worstShift;
    } catch (err) {
        console.error('Summary KPI error:', err);
    }
}

// ══════════════════════════════════════════
// TREND CHART
// ══════════════════════════════════════════
async function loadTrendChart() {
    try {
        const res  = await fetch(`${API_BASE}/trend`);
        const data = await res.json();
        const total = data.data.reduce((a, b) => a + b, 0).toFixed(1);
        const el = document.getElementById('totalWaste');
        if (el) el.innerText = total + ' %';
        if (trendChartInstance) trendChartInstance.destroy();
        const ctx = document.getElementById('trendChart').getContext('2d');
        trendChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [
                    {
                        label: 'Actual Waste %',
                        data: data.data,
                        borderColor: '#357ABD',
                        backgroundColor: 'rgba(53,122,189,0.15)',
                        tension: 0.5,
                        fill: true,
                        pointRadius: 6
                    },
                    {
                        label: 'Predicted Waste %',
                        data: data.predicted,
                        borderColor: '#e53935',
                        backgroundColor: 'rgba(229,57,53,0.08)',
                        tension: 0.5,
                        fill: false,
                        borderDash: [6, 3],
                        pointRadius: 5
                    }
                ]
            },
            options: chartOptions()
        });
    } catch (err) {
        console.error('Trend chart error:', err);
    }
}

// ══════════════════════════════════════════
// SHIFT CHART
// ══════════════════════════════════════════
async function loadShiftChart() {
    try {
        const res  = await fetch(`${API_BASE}/shift`);
        const data = await res.json();
        if (shiftChartInstance) shiftChartInstance.destroy();
        shiftChartInstance = new Chart(
            document.getElementById('shiftChart'), {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Avg Waste % by Shift',
                        data: data.data,
                        backgroundColor: ['#4a90e2', '#f59e0b'],
                        borderRadius: 8
                    }]
                },
                options: chartOptions()
            });
    } catch (err) {
        console.error('Shift chart error:', err);
    }
}

// ══════════════════════════════════════════
// CUTTING METHOD CHART
// ══════════════════════════════════════════
async function loadCuttingMethodChart() {
    try {
        const res  = await fetch(`${API_BASE}/cuttingmethod`);
        const data = await res.json();
        if (cuttingMethodChartInstance) cuttingMethodChartInstance.destroy();
        cuttingMethodChartInstance = new Chart(
            document.getElementById('cuttingMethodChart'), {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Avg Waste % by Cutting Method',
                        data: data.data,
                        backgroundColor: ['#10b981', '#f59e0b'],
                        borderRadius: 8
                    }]
                },
                options: chartOptions()
            });
    } catch (err) {
        console.error('Cutting method error:', err);
    }
}

// ══════════════════════════════════════════
// RISK DISTRIBUTION PIE
// ══════════════════════════════════════════
async function loadRiskDistributionChart() {
    try {
        const res  = await fetch(`${API_BASE}/riskdistribution`);
        const data = await res.json();
        if (riskDistChartInstance) riskDistChartInstance.destroy();
        riskDistChartInstance = new Chart(
            document.getElementById('riskDistributionChart'), {
                type: 'doughnut',
                data: {
                    labels: data.labels,
                    datasets: [{
                        data: data.data,
                        backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                color: '#1f3d7a',
                                font: { weight: 'bold' }
                            }
                        }
                    }
                }
            });
    } catch (err) {
        console.error('Risk distribution error:', err);
    }
}

// ══════════════════════════════════════════
// ACCURACY GAP CHART
// ══════════════════════════════════════════
async function loadAccuracyGapChart() {
    try {
        const res  = await fetch(`${API_BASE}/accuracygap`);
        const data = await res.json();
        if (accuracyGapChartInstance) accuracyGapChartInstance.destroy();
        accuracyGapChartInstance = new Chart(
            document.getElementById('accuracyGapChart'), {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Prediction Gap (|Predicted - Actual|)',
                        data: data.data,
                        borderColor: '#f59e0b',
                        backgroundColor: 'rgba(245,158,11,0.1)',
                        tension: 0.4,
                        fill: true,
                        pointRadius: 5
                    }]
                },
                options: chartOptions()
            });
    } catch (err) {
        console.error('Accuracy gap error:', err);
    }
}

// ══════════════════════════════════════════
// FABRIC CHART
// ══════════════════════════════════════════
async function loadFabricChart() {
    try {
        const res  = await fetch(`${API_BASE}/fabric`);
        const data = await res.json();
        if (fabricChartInstance) fabricChartInstance.destroy();
        fabricChartInstance = new Chart(
            document.getElementById('fabricChart'), {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Waste %',
                        data: data.data,
                        backgroundColor: data.labels.map((_, i) =>
                            `hsla(${210 + i * 20}, 70%, 55%, 0.8)`),
                        borderColor: '#1f3d7a',
                        borderWidth: 2,
                        borderRadius: 8
                    }]
                },
                options: chartOptions()
            });
    } catch (err) {
        console.error('Fabric chart error:', err);
    }
}

// ══════════════════════════════════════════
// STYLE CHART
// ══════════════════════════════════════════
async function loadStyleChart() {
    try {
        const res  = await fetch(`${API_BASE}/style`);
        const data = await res.json();
        if (styleChartInstance) styleChartInstance.destroy();
        styleChartInstance = new Chart(
            document.getElementById('styleChart'), {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Waste %',
                        data: data.data,
                        backgroundColor: data.labels.map((_, i) =>
                            `hsla(${240 + i * 20}, 65%, 55%, 0.8)`),
                        borderColor: '#1f3d7a',
                        borderWidth: 2,
                        borderRadius: 8
                    }]
                },
                options: chartOptions()
            });
    } catch (err) {
        console.error('Style chart error:', err);
    }
}

// ══════════════════════════════════════════
// LAST 5 PREDICTIONS
// ══════════════════════════════════════════
async function loadLast5() {
    try {
        const res  = await fetch(`${API_BASE}/last5`);
        const data = await res.json();
        const container = document.getElementById('last5Container');
        if (data.length === 0) {
            container.innerHTML =
                '<p style="color:#6b7280">No predictions yet.</p>';
            return;
        }
        container.innerHTML = data.map(item => `
            <div class="pred-item">
                <div>
                    <div class="pred-date">${item.date}</div>
                    <div class="pred-value">${item.predicted} %</div>
                </div>
                <span class="badge badge-${item.risk.toLowerCase()}">
                    ${item.risk}
                </span>
            </div>
        `).join('');
    } catch (err) {
        console.error('Last5 error:', err);
    }
}

// ══════════════════════════════════════════
// MODEL ACCURACY
// ══════════════════════════════════════════
async function loadAccuracy() {
    try {
        const res  = await fetch(`${API_BASE}/accuracy`);
        const data = await res.json();
        const container = document.getElementById('accuracyContainer');
        if (data.length === 0) {
            container.innerHTML =
                '<p style="color:#6b7280">No accuracy data.</p>';
            return;
        }
        container.innerHTML = data.map(item => `
            <div class="acc-item">
                <div class="acc-label">
                    <span>${item.material}</span>
                    <span>${item.accuracy.toFixed(1)}%</span>
                </div>
                <div class="acc-bar-bg">
                    <div class="acc-bar-fill"
                         style="width:${item.accuracy}%">
                    </div>
                </div>
            </div>
        `).join('');
    } catch (err) {
        console.error('Accuracy error:', err);
    }
}

// ══════════════════════════════════════════
// HISTORY TABLE
// ══════════════════════════════════════════
async function loadHistory() {
    try {
        const res  = await fetch(`${API_BASE}/history`);
        const data = await res.json();
        const container = document.getElementById('historyContainer');
        if (data.length === 0) {
            container.innerHTML =
                '<p style="color:#6b7280">No history yet.</p>';
            return;
        }
        container.innerHTML = `
            <table class="history-table">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Predicted Waste %</th>
                        <th>Actual Waste %</th>
                        <th>Risk Level</th>
                    </tr>
                </thead>
                <tbody>
                    ${data.map(item => `
                        <tr>
                            <td>${item.date}</td>
                            <td>${item.predicted}</td>
                            <td>${item.actual}</td>
                            <td>
                                <span class="badge 
                                    badge-${item.risk.toLowerCase()}">
                                    ${item.risk}
                                </span>
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (err) {
        console.error('History error:', err);
    }
}

// ══════════════════════════════════════════
// LOAD PREDICTIONS DROPDOWN
// ══════════════════════════════════════════
async function loadPredictionsDropdown() {
    try {
        const res  = await fetch(`${API_BASE}/predictions/list`);
        const data = await res.json();
        const select = document.getElementById('predictionSelect');

        if (data.length === 0) {
            select.innerHTML =
                '<option value="" disabled selected>' +
                'No predictions found</option>';
            return;
        }

        select.innerHTML =
            '<option value="" disabled selected>' +
            'Select a prediction record</option>';

        data.forEach(item => {
            const riskEmoji =
                item.risk === 'Low'    ? '🟢' :
                    item.risk === 'Medium' ? '🟡' : '🔴';
            select.innerHTML +=
                `<option value="${item.id}">
                    ${riskEmoji} ${item.date} 
                    — ${item.predicted}% (${item.risk})
                </option>`;
        });

        select.addEventListener('change', function() {
            const selected = data.find(
                item => item.id === this.value);
            if (selected) {
                const info = document.getElementById(
                    'selectedPredictionInfo');
                info.style.display = 'block';
                info.innerHTML = `
                    <strong>Selected Prediction:</strong><br>
                    📅 Date: ${selected.date}<br>
                    📊 Predicted Waste: 
                        <strong>${selected.predicted}%</strong><br>
                    ⚠️ Risk Level: 
                        <strong>${selected.risk}</strong>
                `;
            }
        });

    } catch (err) {
        console.error('Dropdown load error:', err);
    }
}

// ══════════════════════════════════════════
// SAVE CUTTING RISK RECORD
// ══════════════════════════════════════════
async function saveCuttingRisk() {
    const predictionId     = document.getElementById('predictionSelect').value;
    const noOfLayers       = parseInt(document.getElementById('noOfLayers').value);
    const fabricGsm        = parseInt(document.getElementById('fabricGsm').value);
    const cuttingOverlapMm = parseInt(document.getElementById('cuttingOverlapMm').value);
    const markerEfficiency = parseFloat(document.getElementById('markerEfficiency').value);
    const actualWaste      = parseFloat(document.getElementById('actualWaste').value);
    const cuttingMethod    = document.getElementById('cuttingMethodSelect').value;
    const shift            = document.getElementById('shiftSelect').value;
    const notes            = document.getElementById('notes').value;

    let hasError = false;

    if (!predictionId) {
        document.getElementById('err-prediction').innerText =
            '⚠ Please select a prediction';
        hasError = true;
    } else {
        document.getElementById('err-prediction').innerText = '';
    }

    if (!noOfLayers || noOfLayers <= 0) {
        document.getElementById('err-layers').innerText =
            '⚠ Please enter number of layers';
        hasError = true;
    } else {
        document.getElementById('err-layers').innerText = '';
    }

    if (!cuttingMethod) {
        document.getElementById('err-method').innerText =
            '⚠ Please select cutting method';
        hasError = true;
    } else {
        document.getElementById('err-method').innerText = '';
    }

    if (!shift) {
        document.getElementById('err-shift').innerText =
            '⚠ Please select shift';
        hasError = true;
    } else {
        document.getElementById('err-shift').innerText = '';
    }

    if (hasError) return;

    try {
        const res = await fetch(`${API_BASE}/risk/save`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                predictionId,
                noOfLayers,
                fabricGsm:           isNaN(fabricGsm) ? null : fabricGsm,
                cuttingOverlapMm:    isNaN(cuttingOverlapMm) ? null : cuttingOverlapMm,
                markerEfficiencyPct: isNaN(markerEfficiency) ? null : markerEfficiency,
                actualWastagePct:    isNaN(actualWaste) ? null : actualWaste,
                cuttingMethod,
                shift,
                notes
            })
        });

        const data = await res.json();

        const success = document.getElementById('riskSaveSuccess');
        success.style.display = 'block';
        setTimeout(() => success.style.display = 'none', 3000);

        await loadShiftChart();
        await loadCuttingMethodChart();
        await loadSummaryKPIs();

    } catch (err) {
        console.error('Save risk record error:', err);
        showGlobalError('Failed to save risk record!');
    }
}

// ══════════════════════════════════════════
// RESET RISK FORM
// ══════════════════════════════════════════
function resetRiskForm() {
    document.getElementById('predictionSelect').selectedIndex    = 0;
    document.getElementById('noOfLayers').value                  = '';
    document.getElementById('fabricGsm').value                   = '';
    document.getElementById('cuttingOverlapMm').value            = '';
    document.getElementById('markerEfficiency').value            = '';
    document.getElementById('actualWaste').value                 = '';
    document.getElementById('cuttingMethodSelect').selectedIndex = 0;
    document.getElementById('shiftSelect').selectedIndex         = 0;
    document.getElementById('notes').value                       = '';
    document.getElementById('riskSaveSuccess').style.display     = 'none';
    document.getElementById('selectedPredictionInfo').style.display = 'none';
    document.getElementById('err-prediction').innerText          = '';
    document.getElementById('err-layers').innerText              = '';
    document.getElementById('err-method').innerText              = '';
    document.getElementById('err-shift').innerText               = '';
}

// ══════════════════════════════════════════
// GLOBAL ERROR
// ══════════════════════════════════════════
function showGlobalError(message) {
    const errDiv = document.getElementById('globalError');
    if (!errDiv) return;
    errDiv.innerText = '❌ ' + message;
    errDiv.style.display = 'block';
    setTimeout(() => errDiv.style.display = 'none', 5000);
}

// ══════════════════════════════════════════
// TOAST
// ══════════════════════════════════════════
function showToast() {
    const toast = document.getElementById('toast');
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3000);
}

// ══════════════════════════════════════════
// CHART OPTIONS
// ══════════════════════════════════════════
function chartOptions() {
    return {
        responsive: true,
        plugins: {
            legend: {
                labels: { color: '#1f3d7a', font: { weight: 'bold' } }
            }
        },
        scales: {
            x: {
                ticks: { color: '#1f3d7a', font: { weight: 'bold' } },
                grid:  { color: 'rgba(53,122,189,0.1)' }
            },
            y: {
                ticks: { color: '#1f3d7a', font: { weight: 'bold' } },
                grid:  { color: 'rgba(53,122,189,0.1)' }
            }
        }
    };
}