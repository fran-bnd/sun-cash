package Service;

import DAO.AccountDAO;
import DAO.BankUserDAO;
import DAO.TransactionDAO;
import Model.Account;
import Model.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class AccountService {

    private TransactionDAO transactionDAO;
    private AccountDAO accountDAO;
    public AccountService(TransactionDAO transactionDAO, AccountDAO accountDAO){
        this.transactionDAO = transactionDAO;
        this.accountDAO = accountDAO;

    }
    /**
     * Create a new account for existed user, validate the user before creating new account
     * @param account
     * @return
     */
    public static Account createNewAccount(Account account){
        if(BankUserDAO.isUserValid(account.getUser()))
            return AccountDAO.createNewAccount(account);
        else
            return null;
    }

    /**
     *
     * @param account_user
     * @return
     */

    public static List<Account> getAccountsByUserID(int account_user){
        return AccountDAO.getAccountsByUserID(account_user);
    }

    /**
     * deposit a fund to a user account
     * the deposit amount must be > zero
     * @param account
     * @param amount
     * @return
     */

    public  Account deposit(Account account, double amount){
        if(amount > 0){
            double newAccountBalance = account.getBalance() + amount;
            account.setBalance(newAccountBalance);
            accountDAO.updateAccount(account);

//            Transaction transaction = new Transaction();
//            transaction.setTransactionType("Deposit");
//            transaction.setAmount(amount);
//            transaction.setAccountUser(account.getUser());
//            transaction.setTransactionTime(new Date());
//            transaction.setAccountID(account.getAccount_id());
            Transaction transaction = new Transaction("Deposit", amount, LocalDateTime.now(), account.getUser(), account
                    .getAccount_id());
            transactionDAO.addTransaction(transaction);

        }
        else {
            System.out.println("Deposit amount is not allowed");
        }
        return account;
    }


    /**
     * withdraw a fund from a user account
     * the withdraw amount must be <= user account balance
     * @param account
     * @param amount
     * @return
     */

    public  Account withdraw(Account account, double amount){
        if(amount > account.getBalance()){
            System.out.println("Insufficient fund! Please change the withdraw amount ");
        }

        double newAccountBalance = account.getBalance() - amount;
        AccountDAO.updateAccount(account);
        account.setBalance(newAccountBalance);

//        Transaction transaction = new Transaction();
//        transaction.setTransactionType("Withdrawal");
//        transaction.setAmount(-amount);
//        transaction.setAccountUser(account.getUser());
//        transaction.setTransactionTime(new Date());
//        transaction.setAccountID(account.getAccount_id());

        Transaction transaction = new Transaction("Withdrawal", amount, LocalDateTime.now(), account.getUser(), account
                .getAccount_id());
        transactionDAO.addTransaction(transaction);


        return account;
    }
}
