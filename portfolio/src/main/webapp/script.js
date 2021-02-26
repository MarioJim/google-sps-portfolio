new Splide('#gallery', {
  type: 'loop',
  padding: {right: '5rem', left: '5rem'},
}).mount();

fetch('/hello').then(response => response.text()).then(text => {
  const serverP = document.getElementById('server-p');
  serverP.textContent = text;
});
