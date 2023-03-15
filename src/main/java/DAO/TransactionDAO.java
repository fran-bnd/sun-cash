package DAO;

import Model.Transaction;
import Util.ConnectionSingleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static Connection connection = ConnectionSingleton.getConnection();

    public static Transaction addTransaction(Transaction transaction) {
        try {
            String sql = "INSERT INTO transactions (transaction_type, amount, time, accountID_from" +
                ", accountID_to) VALUES (?,?, DEFAULT,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, transaction.getType().key());
            preparedStatement.setLong(2, transaction.getAmount());
            preparedStatement.setInt(3, transaction.getAccountFrom());
            preparedStatement.setInt(4, transaction.getAccountTo());
            preparedStatement.executeUpdate();

            ResultSet pky = preparedStatement.getGeneratedKeys();
            if (pky.next()) {
                transaction.setID(pky.getInt(1));
                transaction.setTime(pky.getTimestamp("time"));
                return transaction;
            }
        }
        catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    public static List<Transaction> getTransactionsByAccountID(int accountID) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            String sql = "SELECT * FROM transactions WHERE accountID_from = ? OR accountID_to = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, accountID);
            preparedStatement.setInt(2, accountID);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                //create a string type for transaction_type
                Transaction newTransaction = new Transaction(rs.getInt("transactionID"),
                    Transaction.TransactType.fromInt(rs.getInt("transaction_type")), rs.getLong("amount"),
                    rs.getTimestamp("time"),
                    rs.getInt("accountID_from"), rs.getInt("accountID_to"));
                transactions.add(newTransaction);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return transactions;
    }
}
