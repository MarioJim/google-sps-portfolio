new Splide('#gallery', {
  type: 'loop',
  padding: {right: '5rem', left: '5rem'},
}).mount();

const randomFactContainer = document.getElementById('random-fact-container');
document.getElementById('random-fact-btn').
    addEventListener('click', async () => {
      const response = await fetch('/random-fact');
      const factsList = await response.json();
      randomFactContainer.textContent = factsList[Math.floor(
          factsList.length * Math.random())];
    });
