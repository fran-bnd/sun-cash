package DAO;

import Model.Account;
import Util.ConnectionSingleton;

import java.security.PublicKey;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private static Connection connection = ConnectionSingleton.getConnection();

    public static Account createNewAccount(Account account) {
        try {
            String sql = "INSERT INTO account (balance, account_user) VALUES (?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setLong(1, account.getBalance());
            preparedStatement.setInt(2, account.getUser());

            preparedStatement.executeUpdate();
            ResultSet pky = preparedStatement.getGeneratedKeys();
            while(pky.next()){
                account.setAccount_id(pky.getInt(1));
                return account;
            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return  null;
    }

    public static void updateAccountBalance(int accountID, long amount) {
        try {
            String sql = "UPDATE account SET balance = ? WHERE account_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, amount);
            preparedStatement.setInt(2, accountID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static List<Account> getAccountsByUserID(int account_user) {
        List<Account> accounts = new ArrayList<>();
        try {
            String sql = "SELECT * FROM account WHERE account_user = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, account_user);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next())
                accounts.add(
                    new Account(rs.getInt("account_id"), rs.getLong("balance"), rs.getInt("account_user"))
                );
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return accounts;
    }

    public static Account getAccountByID(int accountID) {
        try {
            String sql = "SELECT * FROM account WHERE account_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, accountID);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next())
                return new Account(rs.getInt("account_id"), rs.getLong("balance"),
                    rs.getInt("account_user"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
