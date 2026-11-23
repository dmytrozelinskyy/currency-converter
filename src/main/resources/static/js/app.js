const form = document.getElementById('convert-form');
const result = document.getElementById('result');
const historyList = document.getElementById('history');

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
        loadHistory();
    } catch (err) {
        result.textContent = `Error: ${err.message}`;
    }
});

async function loadHistory() {
    try {
        const res = await fetch('/api/history?size=5');
        if (!res.ok) return;
        const page = await res.json();
        historyList.replaceChildren(
            ...page.content.map(h => {
                const li = document.createElement('li');
                li.textContent = `${h.amount} ${h.from} = ${h.result} ${h.to}`;
                return li;
            }));
    } catch (e) { }
}

loadHistory();