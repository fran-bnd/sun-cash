package DAO;

import Model.Transaction;
import Util.ConnectionSingleton;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static Connection connection = ConnectionSingleton.getConnection();

    public static Transaction addTransaction(Transaction transaction){

        try {
            String sql = "INSERT INTO transactions VALUES(?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, transaction.getTransactionType().toString());
            preparedStatement.setDouble(2, transaction.getAmount());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(transaction.getTransactionTime()));
            preparedStatement.setInt(4, transaction.getAccountUser());
            preparedStatement.setInt(5, transaction.getAccountID());

            preparedStatement.executeUpdate();

            ResultSet pky = preparedStatement.getGeneratedKeys();
            while (pky.next()){
                int generatedTransactionID = pky.getInt(1);
                return new Transaction(generatedTransactionID, transaction.getTransactionType(), transaction.getAmount(),
                        transaction.getTransactionTime(), transaction.getAccountUser(), transaction.getAccountID());
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;

    }



    public static List<Transaction> transactionByUserID(int userId){

        List<Transaction> transactions = new ArrayList<>();

        try {
            String sql = "SELECT * FROM transactions WHERE account_user = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()){
                //create a string type for transaction_type
                String transactionTypeString = rs.getString("transaction_type");
                Transaction newTransaction = new Transaction(rs.getInt("transaction_id"), transactionTypeString,
                        rs.getDouble("amount"), (rs.getDate("transaction_time").toLocalDate().atTime(LocalTime.now())), rs.getInt("account_user"),
                        rs.getInt("account_id"));

                transactions.add(newTransaction);
            }

        } catch (SQLException e){
            System.out.println(e.getMessage());
        }

        return transactions;
    }



}
