// Sustainability Dashboard JavaScript

class SustainabilityDashboard {
    constructor() {
        this.initializeEventListeners();
        this.loadInitialData();
    }

    initializeEventListeners() {
        // Refresh button
        document.getElementById('refreshDashboard')?.addEventListener('click', () => {
            this.refreshDashboard();
        });

        // Date range selector for reports
        document.getElementById('reportDateRange')?.addEventListener('change', (e) => {
            this.updateReportRange(e.target.value);
        });

        // Export buttons
        document.getElementById('exportPDF')?.addEventListener('click', () => {
            this.exportReport('pdf');
        });

        document.getElementById('exportExcel')?.addEventListener('click', () => {
            this.exportReport('excel');
        });

        // Filter by fabric type
        document.getElementById('fabricFilter')?.addEventListener('change', (e) => {
            this.filterByFabric(e.target.value);
        });
    }

    async loadInitialData() {
        try {
            await Promise.all([
                this.loadFabricBreakdown(),
                this.loadImpactTrends(),
                this.loadHighRiskFabrics()
            ]);
        } catch (error) {
            this.showNotification('Error loading dashboard data', 'error');
        }
    }

    async loadFabricBreakdown() {
        try {
            const response = await fetch('/sustainability/api/fabric-breakdown');
            const data = await response.json();
            this.updateFabricBreakdownChart(data);
            this.updateFabricBreakdownTable(data);
        } catch (error) {
            console.error('Error loading fabric breakdown:', error);
        }
    }

    async loadImpactTrends() {
        try {
            const response = await fetch('/sustainability/api/impact-trends?days=30');
            const data = await response.json();
            this.updateImpactChart(data);
        } catch (error) {
            console.error('Error loading impact trends:', error);
        }
    }

    async loadHighRiskFabrics() {
        try {
            const response = await fetch('/sustainability/api/high-risk');
            const data = await response.json();
            this.updateRiskTable(data);
        } catch (error) {
            console.error('Error loading high risk fabrics:', error);
        }
    }

    updateFabricBreakdownChart(data) {
        const ctx = document.getElementById('fabricChart')?.getContext('2d');
        if (!ctx) return;

        if (this.fabricChart) {
            this.fabricChart.destroy();
        }

        this.fabricChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.map(f => f.fabricType),
                datasets: [{
                    data: data.map(f => f.wastagePercentage),
                    backgroundColor: data.map(f => f.colorCode || this.generateColor(f.fabricType)),
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            font: {
                                size: 11
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: (context) => {
                                const fabric = data[context.dataIndex];
                                return [
                                    `${fabric.fabricType}: ${fabric.wastagePercentage.toFixed(1)}%`,
                                    `Waste: ${fabric.totalWasteKg.toFixed(1)} kg`,
                                    `Risk: ${fabric.riskLevel}`
                                ];
                            }
                        }
                    }
                },
                cutout: '60%'
            }
        });
    }

    updateImpactChart(data) {
        const ctx = document.getElementById('impactChart')?.getContext('2d');
        if (!ctx) return;

        if (this.impactChart) {
            this.impactChart.destroy();
        }

        this.impactChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels || ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [
                    {
                        label: 'CO2 Savings (kg)',
                        data: data.co2 || [1200, 1350, 1100, 1600, 1450, 1700],
                        borderColor: '#28a745',
                        backgroundColor: 'rgba(40, 167, 69, 0.1)',
                        tension: 0.4,
                        fill: true
                    },
                    {
                        label: 'Water Saved (10L)',
                        data: data.water || [850, 920, 780, 1100, 980, 1250],
                        borderColor: '#0d6efd',
                        backgroundColor: 'rgba(13, 110, 253, 0.1)',
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0,0,0,0.05)'
                        },
                        title: {
                            display: true,
                            text: 'Amount'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    updateFabricBreakdownTable(data) {
        const tbody = document.getElementById('fabricBreakdownBody');
        if (!tbody) return;

        tbody.innerHTML = '';

        data.sort((a, b) => b.wastagePercentage - a.wastagePercentage).forEach(fabric => {
            const row = document.createElement('tr');
            row.className = fabric.riskLevel === 'HIGH' ? 'table-danger' :
                fabric.riskLevel === 'MEDIUM' ? 'table-warning' : '';

            row.innerHTML = `
                <td><strong>${fabric.fabricType}</strong></td>
                <td>${fabric.wastagePercentage.toFixed(1)}%</td>
                <td><span class="badge bg-${fabric.riskLevel === 'HIGH' ? 'danger' :
                fabric.riskLevel === 'MEDIUM' ? 'warning' : 'success'}">
                    ${fabric.riskLevel}
                </span></td>
                <td>${fabric.totalWasteKg.toFixed(1)} kg</td>
                <td>${fabric.jobCount}</td>
                <td>
                    <button class="btn btn-sm btn-outline-info" onclick="showOptimizationTips('${fabric.fabricType}')">
                        <i class="bi bi-lightbulb"></i>
                    </button>
                </td>
            `;

            tbody.appendChild(row);
        });
    }

    updateRiskTable(data) {
        const container = document.getElementById('riskFabrics');
        if (!container) return;

        container.innerHTML = '';

        data.slice(0, 5).forEach(fabric => {
            const card = document.createElement('div');
            card.className = 'alert ' + (fabric.riskLevel === 'HIGH' ? 'alert-danger' : 'alert-warning') + ' mb-2';
            card.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>${fabric.fabricType}</strong>
                        <br>
                        <small>${fabric.avgWastagePct.toFixed(1)}% wastage • ${fabric.totalWasteKg.toFixed(1)} kg waste</small>
                    </div>
                    <span class="badge bg-${fabric.riskLevel === 'HIGH' ? 'danger' : 'warning'}">${fabric.riskLevel}</span>
                </div>
            `;
            container.appendChild(card);
        });
    }

    async refreshDashboard() {
        this.showLoader();
        await this.loadInitialData();
        this.hideLoader();
        this.showNotification('Dashboard refreshed successfully', 'success');
    }

    async exportReport(format) {
        const startDate = document.getElementById('startDate')?.value ||
            this.getDefaultStartDate();
        const endDate = document.getElementById('endDate')?.value ||
            this.getDefaultEndDate();

        window.location.href = `/sustainability/report/${format}?startDate=${startDate}&endDate=${endDate}`;
    }

    async filterByFabric(fabricType) {
        if (!fabricType) return;

        try {
            const response = await fetch(`/sustainability/api/fabric-details?type=${fabricType}`);
            const data = await response.json();
            this.showFabricDetails(data);
        } catch (error) {
            console.error('Error filtering by fabric:', error);
        }
    }

    showFabricDetails(data) {
        // Implement modal or detailed view
        alert(`Fabric: ${data.fabricType}\nWastage: ${data.wastagePercentage}%\nRecommendations: ${data.optimizationTips?.join(', ')}`);
    }

    showOptimizationTips(fabricType) {
        fetch(`/sustainability/api/optimization-tips?fabric=${fabricType}`)
            .then(response => response.json())
            .then(tips => {
                const tipsList = tips.map(tip => `• ${tip}`).join('\n');
                alert(`Optimization Tips for ${fabricType}:\n\n${tipsList}`);
            });
    }

    updateReportRange(range) {
        const [startDate, endDate] = this.getDateRangeFromPreset(range);
        document.getElementById('startDate').value = startDate;
        document.getElementById('endDate').value = endDate;
    }

    getDateRangeFromPreset(preset) {
        const today = new Date();
        const endDate = this.formatDate(today);

        let startDate = new Date();
        switch(preset) {
            case '7days':
                startDate.setDate(today.getDate() - 7);
                break;
            case '30days':
                startDate.setDate(today.getDate() - 30);
                break;
            case '90days':
                startDate.setDate(today.getDate() - 90);
                break;
            case 'year':
                startDate.setFullYear(today.getFullYear() - 1);
                break;
            default:
                startDate.setDate(today.getDate() - 30);
        }

        return [this.formatDate(startDate), endDate];
    }

    formatDate(date) {
        return date.toISOString().split('T')[0];
    }

    getDefaultStartDate() {
        const date = new Date();
        date.setDate(date.getDate() - 30);
        return this.formatDate(date);
    }

    getDefaultEndDate() {
        return this.formatDate(new Date());
    }

    generateColor(str) {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            hash = str.charCodeAt(i) + ((hash << 5) - hash);
        }

        const hue = Math.abs(hash % 360);
        return `hsl(${hue}, 70%, 50%)`;
    }

    showLoader() {
        const loader = document.getElementById('globalLoader');
        if (loader) {
            loader.style.display = 'block';
        }
    }

    hideLoader() {
        const loader = document.getElementById('globalLoader');
        if (loader) {
            loader.style.display = 'none';
        }
    }

    showNotification(message, type = 'info') {
        const toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) return;

        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type} border-0`;
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');

        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;

        toastContainer.appendChild(toast);

        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();

        setTimeout(() => {
            toast.remove();
        }, 5000);
    }
}

// Initialize dashboard when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.dashboard = new SustainabilityDashboard();
});

// Global functions for HTML onclick handlers
function showOptimizationTips(fabricType) {
    window.dashboard?.showOptimizationTips(fabricType);
}

function downloadReport(format) {
    window.dashboard?.exportReport(format);
}