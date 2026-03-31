import '@vaadin/chart';
import { html, render } from 'lit-html';

const chartData = {
  title: 'Account Balance Overview',
  categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
  data: [1200, 1500, 1100, 1800, 1700, 2000]
};

const chartTemplate = html`
  <vaadin-chart
    title="${chartData.title}"
    categories="${chartData.categories.join(',')}"
    type="column"
    style="width:100%;height:320px;"
  >
    <vaadin-chart-series
      title="Balance"
      values="${chartData.data.join(',')}"
    ></vaadin-chart-series>
  </vaadin-chart>
`;

window.addEventListener('DOMContentLoaded', () => {
  const chartContainer = document.getElementById('dashboard-summary-chart');
  if (chartContainer) {
    render(chartTemplate, chartContainer);
  }
});
