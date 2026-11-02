const form = document.getElementById('convert-form');
const result = document.getElementById('result');

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const amount = document.getElementById('amount').value;
    const from = document.getElementById('from').value.toUpperCase();
    const to = document.getElementById('to').value.toUpperCase();

    result.textContent = 'Converting in progress...';

    try {
        const params = new URLSearchParams({from, to, amount});
        const response = await fetch(`/api/convert?${params}`);

        if (!response.ok)
            throw new Error(`Request failed: ${response.status}`);

        const converted = await response.json();
        result.textContent = `${amount} ${from} = ${converted} ${to}`;
    } catch (err) {
        result.textContent = `Error: ${err.message}`;
    }
});
