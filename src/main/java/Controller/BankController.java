package Controller;

import Model.Account;
import Model.BankUser;
import Model.Transaction;
import Service.AccountService;
import Service.BankUserService;
import Service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

// Defining endpoints and handlers for controller
public class BankController {
    private ObjectMapper mapper;

    /**
     * Endpoints in the startAPI() method, as the test suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        mapper = new ObjectMapper();

        Javalin app = Javalin.create();
        //1. Process registration - POST localhost:8080/register
        app.post("/register", this::registerHandler);

        //2. Process logins- POST localhost:8080/login
        app.post("/login", this::loginHandler);

        /* Create new account */
        app.post("/account/register", this::accountOpenHandler);

        /* Get account */
        app.post("/users/accounts", this::accountGetHandler);
        /* Get transaction by user id*/
        app.get("/transactions/{user_id}", this::getTransactionByUserIdHandler);
        app.post("/transactions/{user_id}", this::addTransactionHandler);

        return app;
    }


    /**
     * 1. Handler to POST and new registration endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     *
     * The body will contain a representation of a JSON Account, but will not contain an user_id.
     *  - The registration will be successful if and only if the username is not blank, the password is at least 4 characters long, and an Account with that username does not already exist. If all these conditions are met, the response body should contain a JSON of the Account, including its account_id. The response status should be 200 OK, which is the default. The new account should be persisted to the database.
     *  - If the registration is not successful, the response status should be 400. (Client error)
     *
     */
    private void registerHandler(Context context) throws JsonProcessingException {
        BankUser user = mapper.readValue(context.body(), BankUser.class);
        BankUser addedUser = BankUserService.addUser(user);

        // if new unique user return JSON BankUser
        if (addedUser != null){
            context.json(mapper.writeValueAsString(addedUser));
            context.status(200);
        } else {
            // else not successful registration
            context.status(400);
        }
    }

    /**
     * 2: API should be able to process User logins.
     * Verify login on the endpoint POST localhost:8080/login.
     * The request body will contain a JSON representation of a BankUser, not containing an user_id. In the future, this action may generate a Session token to allow the user to securely use the site. We will not worry about this for now.
     *   - The login will be successful if and only if the username and password provided in the request body JSON match a real user existing on the database.
     *     If successful, the response body should contain a JSON of the user in the response body, including its user_id.
     *     The response status should be 200 OK, which is the default.
     *   - If the login is not successful, the response status should be 401. (Unauthorized)
     *
     * @param context
     * @throws JsonProcessingException
     */
    private void loginHandler(Context context) throws JsonProcessingException {
        BankUser user = mapper.readValue(context.body(), BankUser.class);
        BankUser loginUser = BankUserService.loginUser(user);

        // if unique user return JSON BankUser
        if (loginUser != null){
            context.json(mapper.writeValueAsString(loginUser));
            context.status(200);
        } else {
            // unauthorized login
            context.status(401);
        }
    }

    /* Get user from request body (JSON) and add user
     * responds with 400 (error) or 200 (success)
     */
    private void accountOpenHandler(Context ctx) throws JsonProcessingException {
        String[] jsonStrings = ctx.body().split("\30", 2);
        BankUser user = mapper.readValue(jsonStrings[0], BankUser.class);
        BankUser loginUser = BankUserService.loginUser(user);
        if (loginUser == null) {
            ctx.status(400);
            return;
        }
        Account account = mapper.readValue(jsonStrings[1], Account.class);
        account.setUser(loginUser.getUser_id());
        Account newAccount = AccountService.createNewAccount(account);
        if (newAccount == null) {
            ctx.status(400);
        } else {
            ctx.json(mapper.writeValueAsString(newAccount));
            ctx.status(200);
        }
    }

    /* Get user's accounts
     * responds with 200 and accounts in body
     */
    private void accountGetHandler(Context ctx) throws JsonProcessingException {
        BankUser user = mapper.readValue(ctx.body(), BankUser.class);
        BankUser loginUser = BankUserService.loginUser(user);
        if (loginUser != null) {
            ctx.json(AccountService.getAccountsByUserID(loginUser.getUser_id()));
            ctx.status(200);
        } else {
            ctx.status(401);
        }
    }

    private void getTransactionByUserIdHandler(Context ctx) throws JsonProcessingException{
        int user_id = Integer.parseInt(ctx.pathParam("user_id"));
        List<Transaction> transactionByUserID = TransactionService.getTransactionByUserID(user_id);
        ctx.json(mapper.writeValueAsString(transactionByUserID));
    }

    private void addTransactionHandler(Context ctx) throws JsonProcessingException{
        Transaction transaction = mapper.readValue(ctx.body(), Transaction.class);
        Transaction newTransaction = TransactionService.addTransaction(transaction);

        if(newTransaction != null){
            ctx.json(mapper.writeValueAsString(newTransaction));
        }
        else {
            ctx.status(400);
        }
    }
}
