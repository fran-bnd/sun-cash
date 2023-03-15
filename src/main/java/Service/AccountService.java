package Service;

import DAO.AccountDAO;
import DAO.BankUserDAO;
import Model.Account;

import java.util.List;

public class AccountService {
    /**
     * Create a new account for existed user, validate the user before creating new account
     * @param account
     * @return
     */
    public static Account createNewAccount(Account account) {
        if(BankUserDAO.isUserValid(account.getUser()))
            return AccountDAO.createNewAccount(account);
        else
            return null;
    }

    /**
     * @param account_user
     * @return
     */

    public static List<Account> getAccountsByUserID(int account_user) {
        return AccountDAO.getAccountsByUserID(account_user);
    }

    public static Account getAccountByID(int accountID) {
        return AccountDAO.getAccountByID(accountID);
    }

    public static void updateAccountBalance(int accountID, long newBalance) {
        AccountDAO.updateAccountBalance(accountID, newBalance);
    }
}
