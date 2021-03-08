const chart = document.getElementById('chart');

Promise.all([
  google.charts.load('current', { packages: ['corechart'] }),
  fetch('/github-languages').then(resp => resp.json()),
]).then(([_, languagesList]) => {
  const data = google.visualization.arrayToDataTable([
    ['Language', 'Percentage'],
    ...languagesList.map((({name, size}) => [name, size])),
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

  const chartInstance = new google.visualization.PieChart(chart);
  chartInstance.draw(data, options);

  document.getElementById('loading').style.display = 'none';
});
