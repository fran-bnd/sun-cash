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
import java.text.ParseException;

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

        //3. Create new account - POST
        app.post("/register/account*", this::accountOpenHandler);

        //4. Retrieve all account by user - GET
        app.get("/users/accounts*", this::accountsGetHandler);

        //5. Retrieve account by accountId - GET
        app.get("/{accountID}/account*", this::accountGetByIDHandler);

        //6. Retrieve transaction by accountId - GET
        app.get("/{accountID}/transactions*", this::getTransactionsHandler);

        //7. Create a new transaction by accountId - Post
        app.post("/{accountID}/transfer*", this::addTransactionHandler);

        return app;
    }

    /**
     * 1. Handler to POST and new registration endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     *
     * The body will contain a representation of a JSON Account, but will not contain a user_id.
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

    /**
     * 3. Get user from request body (JSON) and add user
     * responds with 400 (error) or 200 (success)
     */
    private static String unEncodeReserveChars(String in) {
        String out = new String();
        for (int i = 0; i < in.length(); ++i) {
            char c = in.charAt(i);
            out += c == '%' ? (char)Integer.parseInt(in.substring(++i, ++i + 1), 16) : c;
        }
        return out;
    }

    private static BankUser authUser(String url) {
        int paramsIdx = url.indexOf('?');
        if (paramsIdx < 0 || url.charAt(paramsIdx++) != '?')
            return null;

        String param1 = "username=", param2 = "password=";
        if (!url.regionMatches(paramsIdx, param1, 0, param1.length()))
            return null;
        
        int amperIdx = url.indexOf('&', paramsIdx += param1.length());
        if (amperIdx < 0)
            return null;
        if (!url.regionMatches(amperIdx + 1, param2, 0, param2.length()))
            return null;
        String uname = unEncodeReserveChars(url.substring(paramsIdx, amperIdx)),
            passwd = unEncodeReserveChars(url.substring(amperIdx + 1 + param2.length()));
        BankUser user = new BankUser(uname, passwd);
        /*
        BankUser user = new BankUser(unEncodeReserveChars(url.substring(paramsIdx, amperIdx)),
            unEncodeReserveChars(url.substring(amperIdx + 1 + param2.length())));
            */
        BankUser loginUser = BankUserService.loginUser(user);
        return loginUser;
    }

    private void accountOpenHandler(Context ctx) throws JsonProcessingException {
        BankUser user = authUser(ctx.fullUrl());
        if (user == null) {
            ctx.status(400);
            return;
        }
        Account account = mapper.readValue(ctx.body(), Account.class);
        account.setUser(user.getUser_id());
        Account newAccount = AccountService.createNewAccount(account);
        if (newAccount == null) {
            ctx.status(400);
        } else {
            ctx.json(mapper.writeValueAsString(newAccount));
            ctx.status(200);
        }
    }

    /**
     * 4. Get user's accounts
     * responds with 200 and accounts in body
     */
    private void accountsGetHandler(Context ctx) throws JsonProcessingException {
        BankUser user = authUser(ctx.fullUrl());
        if (user != null) {
            ctx.json(AccountService.getAccountsByUserID(user.getUser_id()));
            ctx.status(200);
        } else {
            ctx.status(401);
        }
    }

    /**
     * 5. Retrieve account by accountId - GET
     * respond 200 if userId matches userId of the account
     * @param ctx
     * @throws JsonProcessingException
     */
    private void accountGetByIDHandler(Context ctx) throws JsonProcessingException {
        BankUser user = authUser(ctx.fullUrl());
        int accountID = Integer.parseInt(ctx.pathParam("accountID"));
        Account account = AccountService.getAccountByID(accountID);
        if (user != null && account != null && user.getUser_id() == account.getUser()) {
            ctx.json(account);
            ctx.status(200);
        } else {
            ctx.status(401);
        }
    }

    /**
     * 6. Retrieve transaction by accountId - GET
     * return 200 only if user and account are valid
     * @param ctx
     * @throws JsonProcessingException
     */
    private void getTransactionsHandler(Context ctx) throws JsonProcessingException {
        BankUser user = authUser(ctx.fullUrl());
        if (user == null) {
            ctx.status(401);
            return;
        }
        int accountID = Integer.parseInt(ctx.pathParam("accountID"));
        Account account = AccountService.getAccountByID(accountID);
        if (account == null || user.getUser_id() != account.getUser()) {
            ctx.status(401);
            return;
        }
        List<Transaction> transactionByUserID = TransactionService.getTransactionsByAccountID(accountID);
        ctx.json(mapper.writeValueAsString(transactionByUserID));
        ctx.status(200);
    }

    /**
     * 7. Create a new transaction by accountId - Post
     * return 200 if valid user and valid transaction
     * @param ctx
     * @throws JsonProcessingException
     */
    private void addTransactionHandler(Context ctx) throws JsonProcessingException {
        BankUser user = authUser(ctx.fullUrl());
        if (user == null) {
            ctx.status(400);
            return;
        }
        int accountID = Integer.parseInt(ctx.pathParam("accountID"));
        Transaction transaction = mapper.readValue(ctx.body(), Transaction.class);
        Transaction newTransaction = TransactionService.addTransaction(transaction, accountID, user.getUser_id());
        if(newTransaction != null) {
            ctx.json(mapper.writeValueAsString(newTransaction));
            ctx.status(200);
        } else {
            ctx.status(400);
        }
    }
}
