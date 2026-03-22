// Sustainability Input Form JavaScript

class SustainabilityInputManager {
    constructor() {
        this.form = document.getElementById('sustainabilityForm');
        this.validationSummary = document.getElementById('validationSummary');
        this.validationMessage = document.getElementById('validationMessage');
        this.submitBtn = document.getElementById('submitBtn');
        this.livePreview = document.getElementById('livePreview');

        this.initializeTooltips();
        this.initializeFormValidation();
        this.initializeRealTimeValidation();
        this.initializeLivePreview();
        this.initializeFormReset();
    }

    initializeTooltips() {
        const tooltips = document.querySelectorAll('[data-bs-toggle="tooltip"]');
        tooltips.forEach(tooltip => new bootstrap.Tooltip(tooltip));
    }

    initializeFormValidation() {
        // Bootstrap form validation
        this.form.addEventListener('submit', (event) => {
            if (!this.form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                this.showValidationSummary('Please fill all required fields correctly');
            }
            this.form.classList.add('was-validated');
        });

        // Real-time validation on input
        const inputs = this.form.querySelectorAll('input, select');
        inputs.forEach(input => {
            input.addEventListener('input', () => this.validateField(input));
            input.addEventListener('change', () => this.validateField(input));
        });
    }

    initializeRealTimeValidation() {
        // Watch for specific fields that need cross-validation
        const recyclingRate = document.querySelector('[name="recyclingRate"]');
        const energyRate = document.querySelector('[name="renewableEnergyPercentage"]');
        const distance = document.querySelector('[name="transportDistanceKm"]');
        const carbonPrice = document.querySelector('[name="carbonCreditPrice"]');
        const disposalMethod = document.querySelector('[name="wasteDisposalMethod"]');

        if (recyclingRate && energyRate) {
            [recyclingRate, energyRate].forEach(field => {
                field.addEventListener('input', () => this.validateCombinedRates());
            });
        }

        if (distance && carbonPrice) {
            [distance, carbonPrice].forEach(field => {
                field.addEventListener('input', () => this.validateTransportCarbon());
            });
        }

        if (disposalMethod && recyclingRate) {
            disposalMethod.addEventListener('change', () => this.validateDisposalRecycling());
        }
    }

    initializeLivePreview() {
        const inputs = this.form.querySelectorAll('input, select');
        inputs.forEach(input => {
            input.addEventListener('input', () => this.updateLivePreview());
            input.addEventListener('change', () => this.updateLivePreview());
        });
    }

    initializeFormReset() {
        const resetBtn = this.form.querySelector('button[type="reset"]');
        if (resetBtn) {
            resetBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.form.reset();
                this.form.classList.remove('was-validated');
                this.validationSummary.style.display = 'none';
                this.resetLivePreview();
            });
        }
    }

    validateField(field) {
        const value = field.value;
        const min = parseFloat(field.min) || -Infinity;
        const max = parseFloat(field.max) || Infinity;

        let isValid = true;
        let message = '';

        if (field.required && !value) {
            isValid = false;
            message = 'This field is required';
        } else if (value && !isNaN(min) && value < min) {
            isValid = false;
            message = `Value must be at least ${min}`;
        } else if (value && !isNaN(max) && value > max) {
            isValid = false;
            message = `Value must not exceed ${max}`;
        }

        // Custom validation based on field type
        if (field.name === 'recyclingRate' && value > 50) {
            this.checkDisposalMethod(value);
        }

        if (field.name === 'renewableEnergyPercentage' && value > 30) {
            this.showSuccessMessage('Good! Renewable energy above 30% qualifies for tax benefits');
        }

        this.updateFieldValidation(field, isValid, message);
        return isValid;
    }

    updateFieldValidation(field, isValid, message) {
        if (isValid) {
            field.classList.remove('is-invalid');
            field.classList.add('is-valid');

            // Remove any existing feedback
            const existingFeedback = field.parentElement.querySelector('.invalid-feedback');
            if (existingFeedback) {
                existingFeedback.remove();
            }
        } else {
            field.classList.remove('is-valid');
            field.classList.add('is-invalid');

            // Add or update feedback message
            let feedback = field.parentElement.querySelector('.invalid-feedback');
            if (!feedback) {
                feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                field.parentElement.appendChild(feedback);
            }
            feedback.textContent = message;
        }
    }

    validateCombinedRates() {
        const recycling = parseFloat(document.querySelector('[name="recyclingRate"]')?.value) || 0;
        const energy = parseFloat(document.querySelector('[name="renewableEnergyPercentage"]')?.value) || 0;
        const total = recycling + energy;

        if (total > 150) {
            this.showValidationSummary(`Combined recycling (${recycling}%) and energy (${energy}%) exceeds 150%. This may affect your sustainability score.`, 'warning');
        } else {
            this.validationSummary.style.display = 'none';
        }
    }

    validateTransportCarbon() {
        const distance = parseFloat(document.querySelector('[name="transportDistanceKm"]')?.value) || 0;
        const carbonPrice = parseFloat(document.querySelector('[name="carbonCreditPrice"]')?.value) || 0;

        if (distance > 1000 && carbonPrice < 50) {
            this.showValidationSummary(`Long distance (${distance}km) requires carbon credit price of at least $50 for optimal scoring. Current: $${carbonPrice}`, 'warning');
        }
    }

    validateDisposalRecycling() {
        const disposalMethod = document.querySelector('[name="wasteDisposalMethod"]')?.value;
        const recyclingRate = parseFloat(document.querySelector('[name="recyclingRate"]')?.value) || 0;

        if (disposalMethod === 'RECYCLE' && recyclingRate < 30) {
            this.showValidationSummary('Recycling method selected but recycling rate is low. Consider increasing to 30%+', 'info');
        }

        if (disposalMethod === 'LANDFILL' && recyclingRate > 30) {
            this.showValidationSummary('High recycling rate with landfill disposal. Consider switching to RECYCLE method', 'suggestion');
        }
    }

    checkDisposalMethod(recyclingRate) {
        const disposalMethod = document.querySelector('[name="wasteDisposalMethod"]')?.value;
        if (recyclingRate > 50 && disposalMethod !== 'RECYCLE') {
            this.showValidationSummary('With recycling rate >50%, RECYCLE disposal method is recommended for maximum sustainability score', 'suggestion');
        }
    }

    showValidationSummary(message, type = 'error') {
        this.validationSummary.style.display = 'block';
        this.validationMessage.textContent = message;

        // Update alert type
        this.validationSummary.className = 'alert';
        switch(type) {
            case 'warning':
                this.validationSummary.classList.add('alert-warning');
                break;
            case 'info':
                this.validationSummary.classList.add('alert-info');
                break;
            case 'suggestion':
                this.validationSummary.classList.add('alert-success');
                break;
            default:
                this.validationSummary.classList.add('alert-danger');
        }
    }

    showSuccessMessage(message) {
        // Show temporary success message
        const toast = document.createElement('div');
        toast.className = 'toast align-items-center text-white bg-success border-0 position-fixed bottom-0 end-0 m-3';
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-check-circle"></i> ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;

        document.body.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast, { delay: 3000 });
        bsToast.show();

        setTimeout(() => toast.remove(), 3500);
    }

    async updateLivePreview() {
        // Collect form data
        const recycling = parseFloat(document.querySelector('[name="recyclingRate"]')?.value) || 0;
        const energy = parseFloat(document.querySelector('[name="renewableEnergyPercentage"]')?.value) || 0;
        const disposalMethod = document.querySelector('[name="wasteDisposalMethod"]')?.value || 'Not selected';
        const waterTreatment = parseFloat(document.querySelector('[name="waterTreatmentRate"]')?.value) || 0;
        const recycledContent = parseFloat(document.querySelector('[name="recycledContentPercentage"]')?.value) || 0;

        // Calculate preview scores
        const baseScore = (recycling * 0.4) + (energy * 0.3) + (waterTreatment * 0.2) + (recycledContent * 0.1);
        const adjustedScore = disposalMethod === 'RECYCLE' ? baseScore + 5 : baseScore;
        const finalScore = Math.min(100, adjustedScore);

        // Determine rating
        let rating = 'BASIC';
        let color = '#6c757d';
        if (finalScore >= 80) {
            rating = 'GOLD';
            color = '#ffc107';
        } else if (finalScore >= 60) {
            rating = 'SILVER';
            color = '#adb5bd';
        } else if (finalScore >= 40) {
            rating = 'BRONZE';
            color = '#cd7f32';
        }

        // Update live preview
        this.livePreview.innerHTML = `
            <div class="preview-score">
                <div class="score-circle" style="background: linear-gradient(135deg, ${color}, ${this.adjustColor(color, 20)})">
                    ${Math.round(finalScore)}
                </div>
                <h5 class="mb-3">${rating} RATING</h5>
            </div>
            <div class="preview-details">
                <div class="preview-item">
                    <span class="preview-label">Recycling Rate:</span>
                    <span class="preview-value">${recycling}%</span>
                </div>
                <div class="preview-item">
                    <span class="preview-label">Renewable Energy:</span>
                    <span class="preview-value">${energy}%</span>
                </div>
                <div class="preview-item">
                    <span class="preview-label">Water Treatment:</span>
                    <span class="preview-value">${waterTreatment}%</span>
                </div>
                <div class="preview-item">
                    <span class="preview-label">Recycled Content:</span>
                    <span class="preview-value">${recycledContent}%</span>
                </div>
                <div class="preview-item">
                    <span class="preview-label">Disposal Method:</span>
                    <span class="preview-value">${disposalMethod}</span>
                </div>
            </div>
            <div class="mt-3 text-center">
                <small class="text-muted">Estimated Sustainability Score: ${finalScore.toFixed(1)}%</small>
            </div>
        `;

        // Fetch real calculation preview from server
        try {
            const response = await fetch(`/sustainability/api/preview-calculation?recyclingRate=${recycling}&renewableEnergy=${energy}&disposalMethod=${disposalMethod}`);
            if (response.ok) {
                const data = await response.json();
                // Update with server data if needed
            }
        } catch (error) {
            console.error('Error fetching preview:', error);
        }
    }

    adjustColor(color, amount) {
        // Simple color adjustment for gradient
        if (color === '#ffc107') return '#e0a800';
        if (color === '#adb5bd') return '#868e96';
        if (color === '#cd7f32') return '#b36b2a';
        return '#5a6268';
    }

    resetLivePreview() {
        this.livePreview.innerHTML = `
            <div class="text-center text-muted py-4">
                <i class="bi bi-arrow-left-circle fs-1"></i>
                <p class="mt-2">Fill the form to see real-time calculations</p>
            </div>
        `;
    }

    async submitForm() {
        if (!this.form.checkValidity()) {
            this.showValidationSummary('Please fill all required fields correctly');
            return;
        }

        this.submitBtn.disabled = true;
        this.submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Calculating...';

        try {
            // Optional: Perform AJAX validation before submission
            const formData = new FormData(this.form);
            const data = Object.fromEntries(formData.entries());

            const response = await fetch('/sustainability/api/validate-input', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                this.form.submit();
            } else {
                const result = await response.json();
                this.showValidationSummary(result.message || 'Validation failed');
                this.submitBtn.disabled = false;
                this.submitBtn.innerHTML = '<i class="bi bi-calculator"></i> Calculate Enhanced Metrics';
            }
        } catch (error) {
            console.error('Error:', error);
            this.showValidationSummary('Network error. Please try again.');
            this.submitBtn.disabled = false;
            this.submitBtn.innerHTML = '<i class="bi bi-calculator"></i> Calculate Enhanced Metrics';
        }
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.sustainabilityInput = new SustainabilityInputManager();
});