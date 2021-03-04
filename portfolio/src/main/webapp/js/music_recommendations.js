const dateFormatter = new Intl.DateTimeFormat('default', {
  weekday: 'long',
  year: 'numeric',
  month: 'long',
  day: 'numeric',
});

const renderComment = (recommendation) => {
  const container = document.createElement('div');
  const username = document.createElement('h2');
  username.textContent = recommendation.username;
  container.appendChild(username);
  const date = document.createElement('p');
  date.classList.add('comment-date');
  date.textContent = dateFormatter.format(new Date(recommendation.date));
  container.appendChild(date);
  const content = document.createElement('p');
  content.classList.add('comment-content');
  content.textContent = recommendation.content;
  container.appendChild(content);
  return container;
};

fetch('/recommendations').
    then(resp => resp.json()).
    then(recommendations => {
      recommendations.map(renderComment).
          forEach(recommendationDiv => {
            document.getElementById('comments-container').
                appendChild(recommendationDiv);
          });
    });
