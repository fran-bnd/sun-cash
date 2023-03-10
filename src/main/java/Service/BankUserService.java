package Service;

import DAO.BankUserDAO;
import Model.BankUser;

public class BankUserService {
    public static BankUser addUser(BankUser bankUser){
        //The registration will be successful if and only if the username is not blank, the password is at least 4 characters long, and an User with that username does not already exist. If all these conditions are met, the response body should contain a JSON of the User, including its user_id. The response status should be 200 OK, which is the default. The new user should be persisted to the database.
        if (bankUser.getUsername() == "" || bankUser.getPassword().length() < 4) return null;

        return BankUserDAO.insertNewUser(bankUser);
    }

    public static BankUser loginUser(BankUser bankUser){
        //The login will be successful if and only if the username and password provided in the request body JSON match a real account existing on the database.

        return BankUserDAO.loginUser(bankUser);
    }


}
