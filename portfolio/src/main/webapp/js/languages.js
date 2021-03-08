const title = document.getElementById('title');
const chartDiv = document.getElementById('chart');
const loadingText = document.getElementById('loading');
const errorMsg = document.getElementById('error-msg');

const renderChart = (languagesList) => {
  chartDiv.innerHTML = '';
  errorMsg.style.display = 'none';
  const data = google.visualization.arrayToDataTable([
    ['Language', 'Percentage'],
    ...languagesList.map((({ name, size }) => [name, size])),
  ]);
  const options = {
    pieHole: 0.4,
    backgroundColor: 'none',
    colors: languagesList.map(l => l.color),
    sliceVisibilityThreshold: 0.01,
    pieSliceText: 'label',
    legend: {
      position: 'right',
      textStyle: {
        color: 'white',
        fontSize: 14,
      },
    },
    chartArea: {
      width: '100%',
    },
    tooltip: {
      text: 'percentage',
    },
  };
  const chart = new google.visualization.PieChart(chartDiv);
  chart.draw(data, options);
  loadingText.style.display = 'none';
};

Promise.all([
  google.charts.load('current', { packages: ['corechart'] }),
  fetch('/github-languages?user=MarioJim', { method: 'POST' })
      .then(resp => resp.json()),
]).then(([_, languagesList]) => {
  renderChart(languagesList);
  document.getElementById('usernameForm').style.display = 'block';
});

document.getElementById('usernameForm').onsubmit = (event) => {
  event.preventDefault();
  loadingText.style.display = 'block';
  const user = document.getElementById('username').value;
  fetch(`/github-languages?user=${user}`, { method: 'POST' })
      .then(resp => resp.json())
      .then((languagesList) => {
        renderChart(languagesList);
        title.textContent = `${user}'s top programming languages`;
      })
      .catch(() => {
        errorMsg.style.display = 'block';
        loadingText.style.display = 'none';
      });
};
