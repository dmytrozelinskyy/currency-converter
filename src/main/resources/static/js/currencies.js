const list = document.getElementById('currency-list');
const filter = document.getElementById('filter');
const count = document.getElementById('count');
const errorBox = document.getElementById('currency-error');

let all = [];

function render(items) {
    list.replaceChildren(...items.map(c => {
        const li = document.createElement('li');
        const code = document.createElement('strong');
        code.textContent = c.code;
        li.append(code, ` ${c.name}`);
        return li;
    }));
    count.textContent = `${items.length} of ${all.length} currencies`;
}

filter.addEventListener('input', () => {
   const q = filter.value.trim().toLowerCase();
   render(all.filter(c => c.code.toLowerCase().includes(q) || c.name.toLowerCase().includes(q)));
});

async function load() {
    try {
        const res = await fetch('/api/currencies');
        if (!res.ok) {
            const p = await res.json().catch(() => ({}));
            throw new Error(p.detail || `Request failed: ${res.status}`);
        }
        all = await res.json();
        render(all);
    } catch (error) {
        errorBox.hidden = false;
        errorBox.textContent = `Error: ${error.message}`;
    }
}

load();