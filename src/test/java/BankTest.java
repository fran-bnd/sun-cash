import Controller.BankController;
import Model.BankUser;
import Model.Account;
import Model.Transaction;
import Model.Transaction.TransactType;
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

import javax.swing.plaf.multi.MultiPanelUI;

import java.sql.Timestamp;

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
		.uri(URI.create("http://localhost:9001/register/account?username=user&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"balance\": 1000}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		Account expected = new Account(1, 1000, 1);
		Account actual = mapper.readValue(response.body().toString(), Account.class);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void escapeURLTest() throws IOException, InterruptedException {
		HttpRequest postRequest = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:9001/register"))
			.POST(HttpRequest.BodyPublishers.ofString(
				"{\"username\": \"user#2\", \"password\": \"money$$$\"}"
			)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		BankUser expectedUser = new BankUser(1, "user#2", "money$$$");
		BankUser actualUser = mapper.readValue(response.body().toString(), BankUser.class);
		Assert.assertEquals(expectedUser, actualUser);

		postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/register/account?username=user%232&password=money%24%24%24"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"balance\": 1000}"
		)).header("Content-Type", "application/json").build();
		response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		Account expected = new Account(1, 1000, 1);
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
		.uri(URI.create("http://localhost:9001/register/account?username=notuser&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"balance\": 1000}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/*
	* Tries to register a new account under a BankUser using the wrong password
	* Should respond 400
	*/
	@Test
	public void newAccountCredentialTest() throws IOException, InterruptedException {
		registerTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/register/account?username=user&password=wrongpassword"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"balance\": 1000}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/*
	 * Opens several accounts for a user
	 */
	@Test
	public void multipleAccountsTest() throws IOException, InterruptedException {
		newAccountTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/register/account?username=user&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"balance\": 2300}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		Account expected = new Account(2, 2300, 1);
		Account actual = mapper.readValue(response.body().toString(), Account.class);
		Assert.assertEquals(expected, actual);

		postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/register/account?username=user&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"balance\": 10095}"
		)).header("Content-Type", "application/json").build();
		response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		expected = new Account(3, 10095, 1);
		actual = mapper.readValue(response.body().toString(), Account.class);
		Assert.assertEquals(expected, actual);
	}

	/*
	 * GET all accounts for a given user
	 * Should reply with all accounts we added
	 */
	@Test
	public void getUserAccountsTest() throws IOException, InterruptedException {
		multipleAccountsTest();
		HttpRequest getRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/users/accounts?username=user&password=password"))
		.build();
		HttpResponse response = webClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		Account[] expected = {new Account(1, 1000, 1), new Account(2, 2300, 1), new Account(3, 10095, 1)};
		List<Account> actual = mapper.readValue(response.body().toString(), new TypeReference<List<Account>>(){});
		Assert.assertEquals(3, actual.size());
		for (Account account : expected)
			Assert.assertTrue(actual.contains(account));
	}

	/*
	 * Creates a new transaction
	 * Should respond with 200 and created transaction
	 */
	@Test
	public void newTransactionTest() throws IOException, InterruptedException {
		newAccountTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/transfer?username=user&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"type\": \"DEPOSIT\", \"amount\": 1000, \"accountFrom\": 1, \"accountTo\": 1}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		long time = System.currentTimeMillis();
		Assert.assertEquals(200, response.statusCode());
		Transaction expectedTransaction = new Transaction(1, TransactType.DEPOSIT, 1000, new Timestamp(time), 1, 1);
		Transaction actualTransaction = mapper.readValue(response.body().toString(), Transaction.class);
		Assert.assertEquals(expectedTransaction, actualTransaction);

		HttpRequest getRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/account?username=user&password=password"))
		.build();
		response = webClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		Account expectedAccount = new Account(1, 2000, 1);
		Account actualAccount = mapper.readValue(response.body().toString(), Account.class);
		Assert.assertEquals(expectedAccount, actualAccount);
	}

	/*
	 * Test ability to transfer betwee accounts
	 */
	@Test
	public void transferTest() throws IOException, InterruptedException {
		multipleAccountsTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/3/transfer?username=user&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"type\": \"TRANSFER\", \"amount\": 1000, \"accountFrom\": 3, \"accountTo\": 2}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		long time = System.currentTimeMillis();
		Assert.assertEquals(200, response.statusCode());
		Transaction expectedTransaction = new Transaction(1, TransactType.TRANSFER, 1000, new Timestamp(time), 3, 2);
		Transaction actualTransaction = mapper.readValue(response.body().toString(), Transaction.class);
		Assert.assertEquals(expectedTransaction, actualTransaction);

		HttpRequest getRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/2/account?username=user&password=password"))
		.build();
		response = webClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		Account expectedAccount = new Account(2, 3300, 1);
		Account actualAccount = mapper.readValue(response.body().toString(), Account.class);
		Assert.assertEquals(expectedAccount, actualAccount);

		getRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/3/account?username=user&password=password"))
		.build();
		response = webClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		expectedAccount = new Account(3, 9095, 1);
		actualAccount = mapper.readValue(response.body().toString(), Account.class);
		Assert.assertEquals(expectedAccount, actualAccount);
	}

	/*
	 * Creates multiple transactions
	 */
	@Test
	public void multipleTransactionsTest() throws IOException, InterruptedException {
		newTransactionTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/transfer?username=user&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"type\": \"DEPOSIT\", \"amount\": 200, \"accountFrom\": 1, \"accountTo\": 1}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		long time = System.currentTimeMillis();
		Assert.assertEquals(200, response.statusCode());
		Transaction actual = mapper.readValue(response.body().toString(), Transaction.class);
		Transaction expected = new Transaction(2, TransactType.DEPOSIT, 200, new Timestamp(time), 1, 1);
		Assert.assertEquals(expected, actual);

		postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/transfer?username=user&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"type\": \"WITHDRAWL\", \"amount\": 500, \"accountFrom\": 1, \"accountTo\": 1}"
		)).header("Content-Type", "application/json").build();
		response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		time = System.currentTimeMillis();
		Assert.assertEquals(200, response.statusCode());
		actual = mapper.readValue(response.body().toString(), Transaction.class);
		expected = new Transaction(3, TransactType.WITHDRAWL, 500, new Timestamp(time), 1, 1);
		Assert.assertEquals(expected, actual);

		HttpRequest getRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/account?username=user&password=password"))
		.build();
		response = webClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, response.statusCode());
		Account expectedAccount = new Account(1, 1700, 1);
		Account actualAccount = mapper.readValue(response.body().toString(), Account.class);
		Assert.assertEquals(expectedAccount, actualAccount);
	}

	/*
	* Tries to make a transaction with invalid user
	* Should respond 400
	*/
	@Test
	public void transactionInvalidUserTest() throws IOException, InterruptedException {
		newAccountTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/transfer?username=notuser&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"type\": \"DEPOSIT\", \"amount\": 1000, \"accountFrom\": 1, \"accountTo\": 1}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/*
	* Tries to make a transaction with wrong credentials
	* Should respond 400
	*/
	@Test
	public void transactionCredentialsTest() throws IOException, InterruptedException {
		newAccountTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/transfer?username=user&password=wrongpassword"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"type\": \"DEPOSIT\", \"amount\": 1000, \"accountFrom\": 1, \"accountTo\": 1}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/*
	* Tryes to make a transaction from an invalid account
	* Should respond 400
	*/
	@Test
	public void transactionNonExistantFromAccountTest() throws IOException, InterruptedException {
		newAccountTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/transfer?username=user&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"type\": \"DEPOSIT\", \"amount\": 1000, \"accountFrom\": 2, \"accountTo\": 1}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/*
	* Tryes to make a transaction with invalid recipiant account
	* Should respond 400
	*/
	@Test
	public void transactionNonExistantToAccountTest() throws IOException, InterruptedException {
		newAccountTest();
		HttpRequest postRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/transfer?username=user&password=password"))
		.POST(HttpRequest.BodyPublishers.ofString(
			"{\"type\": \"DEPOSIT\", \"amount\": 1000, \"accountFrom\": 1, \"accountTo\": 2}"
		)).header("Content-Type", "application/json").build();
		HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(400, response.statusCode());
	}

	/*
	 * Should return all transactions created
	 */
	@Test
	public void getTransactionByAccountIdTest() throws IOException, InterruptedException {
		multipleTransactionsTest();
		HttpRequest getTransactionByUserIdRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/1/transactions?username=user&password=password"))
		.build();
		HttpResponse getResponse = webClient.send(getTransactionByUserIdRequest, HttpResponse.BodyHandlers.ofString());
		Assert.assertEquals(200, getResponse.statusCode());
		List<Transaction> actual = mapper.readValue(getResponse.body().toString(), new TypeReference<List<Transaction>>(){});
		Assert.assertEquals(3, actual.size());
	}

	/*
	 * Should return all transactions created
	 */
	@Test
	public void getTransfersTest() throws IOException, InterruptedException {
		transferTest();
		HttpRequest getTransactionByUserIdRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/2/transactions?username=user&password=password"))
		.build();
		HttpResponse getResponse = webClient.send(getTransactionByUserIdRequest, HttpResponse.BodyHandlers.ofString());
		long time = System.currentTimeMillis();
		Assert.assertEquals(200, getResponse.statusCode());
		Transaction expected = new Transaction(1, TransactType.TRANSFER, 1000, new Timestamp(time), 3, 2);
		List<Transaction> actual = mapper.readValue(getResponse.body().toString(), new TypeReference<List<Transaction>>(){});
		Assert.assertEquals(1, actual.size());
		Assert.assertTrue(actual.contains(expected));

		getTransactionByUserIdRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://localhost:9001/3/transactions?username=user&password=password"))
		.build();
		getResponse = webClient.send(getTransactionByUserIdRequest, HttpResponse.BodyHandlers.ofString());
		time = System.currentTimeMillis();
		Assert.assertEquals(200, getResponse.statusCode());
		expected = new Transaction(1, TransactType.TRANSFER, 1000, new Timestamp(time), 3, 2);
		actual = mapper.readValue(getResponse.body().toString(), new TypeReference<List<Transaction>>(){});
		Assert.assertEquals(1, actual.size());
		Assert.assertTrue(actual.contains(expected));
	}
}
