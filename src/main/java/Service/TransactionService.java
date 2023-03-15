package Service;

import DAO.TransactionDAO;
import Model.*;
import Model.Transaction.TransactType;

import java.util.List;

public class TransactionService {
    /**
     * @param userId
     * @return a list of transaction by user id
     */
    public static List<Transaction> getTransactionsByAccountID(int accountID) {
        return TransactionDAO.getTransactionsByAccountID(accountID);
    }

    public static Transaction addTransaction(Transaction transaction, int accountID_from, int userID_from) {
        long amount = transaction.getAmount();
        int to_id = transaction.getAccountTo();
        if (transaction.getType() != TransactType.TRANSFER && accountID_from != to_id ||
            accountID_from != transaction.getAccountFrom() || amount < 0)
            return null;
        Account from = AccountService.getAccountByID(accountID_from);
        if (from == null || userID_from != from.getUser() ||
            transaction.getType() != Transaction.TransactType.DEPOSIT &&
            from.getBalance() < amount)
            return null;
        Account to = AccountService.getAccountByID(to_id);
        if (to == null)
            return null;
        AccountService.updateAccountBalance(accountID_from, from.getBalance() +
            (transaction.getType() == TransactType.DEPOSIT ? amount : -amount));
        if (transaction.getType() == TransactType.TRANSFER)
            AccountService.updateAccountBalance(to_id, to.getBalance() + amount);
        return TransactionDAO.addTransaction(transaction);
    }
}
