import Controller.BankController;
import Util.ConnectionSingleton;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) throws IOException, InterruptedException {
//        this line is just for testing that your tables get set up correctly
//        if not, you'll get a stack trace
        ConnectionSingleton.getConnection();

//      All needs to start using the API
        ConnectionSingleton.resetTestDatabase();
        BankController bankController = new BankController();
        HttpClient webClient = HttpClient.newHttpClient();;
        ObjectMapper objectMapper = new ObjectMapper();;
        Javalin app = bankController.startAPI();;
        app.start(8080);

        // Interactive menu: 1. Create User
        Scanner sc = new Scanner(System.in);
        System.out.println("1. Create User");
        System.out.println("1.1 Insert username:");
        String username1 = sc.nextLine();
        System.out.println("1.2 Insert password:");
        String password1 = sc.nextLine();

        // Call API to create new user in DDBB
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"" + username1 + "\", " +
                        "\"password\": \"" + password1 + "\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        System.out.println("API Status: " + status);

        // Interactive menu continuation: 2. User Login
        System.out.println("2. User Login");
        System.out.println("2.1 Insert User username:");
        String loginUsername = sc.nextLine();
        System.out.println("2.2 Insert User password:");
        String loginPassword = sc.nextLine();

        // Call API to login into user in DDBB
        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/login"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"" + loginUsername + "\", " +
                        "\"password\": \"" + loginPassword + "\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse loginResponse = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        int loginStatus = loginResponse.statusCode();
        System.out.println("API Login Status: " + loginStatus);
    }
}
