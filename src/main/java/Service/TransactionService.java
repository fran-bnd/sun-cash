package Service;


import DAO.TransactionDAO;
import Model.Transaction;

import java.util.List;

public class TransactionService {
    /**
     * @param userId
     * @return a list of transaction by user id
     */
    public static List<Transaction> getTransactionByUserID(int userId) {
        return TransactionDAO.transactionByUserID(userId);
    }

    public static Transaction addTransaction(Transaction transaction) {
       return TransactionDAO.addTransaction(transaction);
    }
}



