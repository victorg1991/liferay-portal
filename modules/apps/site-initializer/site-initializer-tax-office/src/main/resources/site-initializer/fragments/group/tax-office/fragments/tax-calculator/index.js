const brackets = [
	{limit: 12450, rate: 0.19},
	{limit: 20200, rate: 0.24},
	{limit: 35200, rate: 0.3},
	{limit: 60000, rate: 0.37},
	{limit: 300000, rate: 0.45},
	{limit: Infinity, rate: 0.47},
];

const currencyFormat = new Intl.NumberFormat('es-ES', {
	currency: 'EUR',
	style: 'currency',
});

const incomeInput = fragmentElement.querySelector('.tax-calculator-income');
const rateElement = fragmentElement.querySelector('.tax-calculator-rate');
const resultElement = fragmentElement.querySelector('.tax-calculator-result');

function calculateTax(income) {
	let lowerLimit = 0;
	let tax = 0;

	for (const bracket of brackets) {
		if (income <= lowerLimit) {
			break;
		}

		tax += (Math.min(income, bracket.limit) - lowerLimit) * bracket.rate;

		lowerLimit = bracket.limit;
	}

	return tax;
}

function updateResult() {
	const income = Math.max(Number(incomeInput.value) || 0, 0);

	const tax = calculateTax(income);

	resultElement.textContent = currencyFormat.format(tax);

	rateElement.textContent = income
		? `Tipo medio: ${((tax / income) * 100).toFixed(2)} %`
		: '';
}

function updateDeadline() {
	const deadlineElement = fragmentElement.querySelector(
		'.tax-calculator-deadline'
	);

	if (!window.dayjs) {
		deadlineElement.textContent =
			'No se pudo cargar dayjs para calcular el plazo.';

		return;
	}

	const today = window.dayjs().startOf('day');

	let deadline = today.month(5).date(30);

	if (today.isAfter(deadline)) {
		deadline = deadline.add(1, 'year');
	}

	deadlineElement.textContent = `Quedan ${deadline.diff(today, 'day')} días para el fin de la campaña de la renta (${deadline.format('DD/MM/YYYY')}).`;
}

incomeInput.addEventListener('input', updateResult);

fragmentElement
	.querySelector('.tax-calculator-form')
	.addEventListener('submit', (event) => event.preventDefault());

updateResult();

if (window.dayjs) {
	updateDeadline();
}
else {
	window.addEventListener('load', updateDeadline);
}
