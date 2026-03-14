// ===== NEOBANK CURRENCY JAVASCRIPT =====

// Convert currency using the live API endpoint
async function convertCurrency() {
    const amount = document.getElementById('convertAmount').value;
    const fromCurrency = document.getElementById('fromCurrency').value;
    const toCurrency = document.getElementById('toCurrency').value;

    if (!amount || parseFloat(amount) <= 0) {
        alert('Please enter a valid amount.');
        return;
    }

    if (fromCurrency === toCurrency) {
        showResult(amount, toCurrency, '1.0000', fromCurrency, amount);
        return;
    }

    try {
        const response = await fetch(
            `/currency/convert?from=${fromCurrency}&to=${toCurrency}&amount=${amount}`
        );

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await response.json();

        showResult(
            data.convertedAmount,
            data.to,
            data.rate,
            data.from,
            data.amount
        );
    } catch (error) {
        console.error('Conversion error:', error);
        alert('Could not perform conversion. Please try again.');
    }
}

function showResult(convertedAmount, toCurrency, rate, fromCurrency, originalAmount) {
    const resultDiv = document.getElementById('conversionResult');
    const resultAmount = document.getElementById('resultAmount');
    const resultCurrency = document.getElementById('resultCurrency');
    const rateInfo = document.getElementById('rateInfo');

    if (resultDiv && resultAmount && resultCurrency && rateInfo) {
        resultAmount.textContent = parseFloat(convertedAmount).toFixed(4);
        resultCurrency.textContent = toCurrency;
        rateInfo.textContent = `1 ${fromCurrency} = ${parseFloat(rate).toFixed(6)} ${toCurrency}`;

        resultDiv.style.display = 'block';
        resultDiv.style.animation = 'fadeIn 0.3s ease';
    }
}

// Swap currencies
function swapCurrencies() {
    const fromSelect = document.getElementById('fromCurrency');
    const toSelect = document.getElementById('toCurrency');

    if (fromSelect && toSelect) {
        const temp = fromSelect.value;
        fromSelect.value = toSelect.value;
        toSelect.value = temp;

        // Re-convert if there's an amount
        const amount = document.getElementById('convertAmount').value;
        if (amount && parseFloat(amount) > 0) {
            convertCurrency();
        }
    }
}

// Refresh exchange rates table
async function refreshRates() {
    const baseCurrency = 'USD';

    try {
        const response = await fetch(`/currency/rates?base=${baseCurrency}`);
        if (!response.ok) throw new Error('Network error');

        const data = await response.json();
        const ratesGrid = document.getElementById('ratesGrid');

        if (ratesGrid && data.rates) {
            ratesGrid.innerHTML = '';

            Object.entries(data.rates).forEach(([currency, rate]) => {
                const rateCard = document.createElement('div');
                rateCard.className = 'rate-card neu-mini-card';
                rateCard.innerHTML = `
                    <div class="rate-currency">
                        <span class="currency-code">${currency}</span>
                    </div>
                    <div class="rate-value">${parseFloat(rate).toFixed(4)}</div>
                    <div class="rate-label">per ${baseCurrency}</div>
                `;
                ratesGrid.appendChild(rateCard);
            });
        }
    } catch (error) {
        console.error('Failed to refresh rates:', error);
    }
}

// Auto-refresh rates every 60 seconds
setInterval(refreshRates, 60000);

// Add fadeIn animation
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }
`;
document.head.appendChild(style);
