const loginForm = document.getElementById('loginForm');

      loginForm.addEventListener('submit', function(event) {
        event.preventDefault(); // prevent the default form submission behavior

        const formData = new FormData(loginForm);
        console.log([...formData.entries()]);

        fetch('http://localhost:8080/login', {
          method: 'POST',
          body: formData
        })
        .then(response => response.json())
        .then(data => {
          console.log(data); // log the response from the server
          // do something with the response, such as redirect to a new page
        })
        .catch(error => {
          console.error(error); // log any errors that occur
          // do something to handle the error, such as display an error message
        });
      });