const body = document.getElementById('history-body');
const empty = document.getElementById('history-empty');
const prev = document.getElementById('prev');
const next = document.getElementById('next');
const pageInfo = document.getElementById('page-info');

const PAGE_SIZE = 20;
let current = 0;

const dateFormat = new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
});

function cell(text, className) {
    const td = document.createElement('td');
    td.textContent = text;
    if (className) td.className = className;
    return td;
}

async function load(page) {
    try {
        const res = await fetch(`/api/history?page=${page}&size=${PAGE_SIZE}`);
        if (!res.ok)
            throw new Error(`Request failed: ${res.status}`);

        const data = await res.json();
        current = data.page.number;

        body.replaceChildren(...data.content.map(h => {
            const tr = document.createElement('tr');
            tr.append(
                cell(dateFormat.format(new Date(h.convertedAt))),
                cell(h.from), cell(h.to),
                cell(h.amount, 'num'), cell(h.result, 'num')
            );
            return tr;
        }));

        const total = data.page.totalPages;
        empty.hidden = data.content.length > 0;
        prev.disabled = current <= 0;
        next.disabled = current + 1 >= total;
        pageInfo.textContent = total > 0 ? `Page ${current + 1} of ${total}` : '';
    } catch (err) {
        empty.hidden = false;
        empty.textContent = `Error: ${err.message}`;
    }
}

prev.addEventListener('click', () => load(current - 1));
next.addEventListener('click', () => load(current + 1));

load(0);