document.getElementById('convert-from'),addEventListener('submit', async (e) => {
    e.preventDefault();

    const amount = document.getElementById('amount').value;
    const from = document.getElementById('from').value.toUpperCase();
    const to = document.getElementById('to').value.toUpperCase();
    const result = document.getElementById('result');

    result.textContent = "Converting in progress...";

    try {
        const response = await fetch(`/api/convert?from=${from}&to=${to}&amount=${amount}`);

        if (!response.ok)
            throw new Error(`Request failed: ${response.status}`);

        const converted = await response.json();
        result.textContent = `${amount} ${from} = ${converted} ${to}`;
    } catch (err) {
        result.textContent = `Error: ${err.message}`;
    }
});x