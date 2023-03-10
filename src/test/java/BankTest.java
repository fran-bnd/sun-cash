import Controller.BankController;
import Model.BankUser;
import Model.Account;
import Model.Transaction;
import Util.ConnectionSingleton;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class BankTest {
	BankController bankControl;
	HttpClient webClient;
	ObjectMapper mapper;
	Javalin app;

	@Before
	public void setUp() throws InterruptedException {
		ConnectionSingleton.resetTestDatabase();
		bankControl = new BankController();
		app = bankControl.startAPI();
		webClient = HttpClient.newHttpClient();
		mapper = new ObjectMapper();
		app.start(9001);
		Thread.sleep(1000);
	}

	@After
	public void stop() {
		app.stop();
	}

	/**
	* POST to localhost:9001/register
	* Valid new user (username = "user", password = "password") should respond 200 with user as body.
	*/
	@Test
	public void registerTest() throws IOException, InterruptedException {
		HttpRequest postRequest = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:9001/register"))
			.POST(HttpRequest.BodyPublishers.ofString(
				"{\"username\": \"user\", \"password\": \"password\"}"
			)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		BankUser expectedUser = new BankUser(1, "user", "password");
		BankUser actualUser = mapper.readValue(response.body().toString(), BankUser.class);
		Assert.assertEquals(expectedUser, actualUser);
	}

	/**
	* A blank username (username = "") should respond 400
	*/
	@Test
	public void registerBlankUsernameTest() throws IOException, InterruptedException {
		HttpRequest postRequest = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:9001/register"))
			.POST(HttpRequest.BodyPublishers.ofString(
				"{\"username\": \"\", \"password\": \"password\"}"
			)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/**
	* A password length < 4 should respond 400.
	*/
	@Test
	public void registerShortPasswordTest() throws IOException, InterruptedException {
		HttpRequest postRequest = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:9001/register"))
			.POST(HttpRequest.BodyPublishers.ofString(
				"{\"username\": \"user\", \"password\": \"p\"}"
			)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/**
	* Trying to add a user with duplicate username should respond 400.
	*/
	@Test
	public void registerDuplicateUserTest() throws IOException, InterruptedException {
		registerTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:9001/register"))
			.POST(HttpRequest.BodyPublishers.ofString(
				"{\"username\": \"\", \"password\": \"password\"}"
			)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/**
	* POST to localhost:9001/login
	* Valid existing user (register user first) should respond 200 with user as body.
	*/
	@Test
	public void loginTest() throws IOException, InterruptedException {
		registerTest();
		HttpRequest postLoginRequest = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:9001/login"))
			.POST(HttpRequest.BodyPublishers.ofString(
			"{\"username\": \"user\", \"password\": \"password\"}"
			)).header("Content-Type", "application/json").build();
		HttpResponse loginResponse = webClient.send(postLoginRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, loginResponse.statusCode());
		BankUser expectedUser = new BankUser(1, "user", "password");
		Assert.assertEquals(expectedUser, mapper.readValue(loginResponse.body().toString(), BankUser.class));
	}
	
	/**
	* Login to nonexisting account should fail (respond 401).
	*/
	@Test
	public void loginNonexistTest() throws IOException, InterruptedException {
		HttpRequest postRequest = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:9001/login"))
			.POST(HttpRequest.BodyPublishers.ofString(
				"{\"username\": \"user\", \"password\": \"password\"}"
			)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(401, response.statusCode());
	}

	/**
	* Login attempt with incorrect password should fail (respond 401).
	*/
	@Test
	public void loginIncorrectPasswordTest() throws IOException, InterruptedException {
		registerTest();
		HttpRequest postLoginRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/login"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"username\": \"user\", \"password\": \"wrongpassword\"}"
		)).header("Content-Type", "application/json").build();
		HttpResponse loginResponse = webClient.send(postLoginRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(401, loginResponse.statusCode());
	}
	
	/**
	* Account enpoints
	* Registers a new account under a BankUser
	* Should respond 200
	*/
	@Test
	public void newAccountTest() throws IOException, InterruptedException {
		registerTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/account/register"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"username\": \"user\", \"password\": \"password\"}" + '\30' +
			"{\"balance\": 10.0}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		Account expected = new Account(1, 10.f, 1);
		Account actual = mapper.readValue(response.body().toString(), Account.class);
		Assert.assertEquals(expected, actual);
	}

	/*
	* Tries to register a new account under a BankUser that doesn't exist
	* Should respond 400
	*/
	@Test
	public void newAccountInvalidUserTest() throws IOException, InterruptedException {
		registerTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/account/register"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"username\": \"notuser\", \"password\": \"password\"}" + '\30' +
			"{\"balance\": 10.0}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/*
	* Tries to register a new account under a BankUser using the wrong password
	* Should respond 400
	*/
	@Test
	public void newAccountWrongCredTest() throws IOException, InterruptedException {
		registerTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/account/register"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"username\": \"user\", \"password\": \"wrongpassword\"}" + '\30' +
			"{\"balance\": 10.0}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/*
	 * GET all accounts for a given user
	 */
	@Test
	public void getUserAccountsTest() throws IOException, InterruptedException {
		newAccountTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/users/accounts"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"username\": \"user\", \"password\": \"password\"}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		Account expected = new Account(1, 10.f, 1);
		List<Account> actual = mapper.readValue(response.body().toString(), new TypeReference<List<Account>>(){});
		Assert.assertEquals(1, actual.size());
		Assert.assertTrue(actual.contains(expected));
	}

	/* FAILING
	 *
	 */
	@Test
	public void getTransactionByUserIdEmptyTest() throws IOException, InterruptedException{
		HttpRequest getTransactionByUserIdRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/transactions/1"))
		.build();
		HttpResponse getTransactionByUserResponse = webClient.send(getTransactionByUserIdRequest, HttpResponse.BodyHandlers.ofString());
		int getTransactionByUserIdStatus = getTransactionByUserResponse.statusCode();
		//the response status should be 200
		Assert.assertEquals(200, getTransactionByUserIdStatus);
		List<Transaction> transactions = mapper.readValue(getTransactionByUserResponse.body().toString(), new TypeReference<List<Transaction>>(){});

		Assert.assertTrue(transactions.isEmpty());
	}
}
