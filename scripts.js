function login() {
            const form = document.getElementById("loginForm");
            const formData = new FormData(form);

            const xhr = new XMLHttpRequest();
            xhr.open("POST", "http://localhost:8080/login");
            xhr.setRequestHeader("Content-Type", "application/json");
            xhr.onreadystatechange = function() {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    if (xhr.status === 200) {
                        console.log(xhr.responseText);
                    } else {
                        console.error(xhr.statusText);
                    }
                }
            };
            const data = JSON.stringify({
                username: formData.get("username"),
                password: formData.get("password")
            });
            xhr.send(data);
        }