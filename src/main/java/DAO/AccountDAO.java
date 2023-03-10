package DAO;

import Model.Account;
import Util.ConnectionSingleton;

import java.security.PublicKey;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private static Connection connection = ConnectionSingleton.getConnection();

    public static Account createNewAccount(Account account){
        try {
            String sql = "INSERT INTO account (balance, account_user) VALUES (?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setDouble(1, account.getBalance());
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

    public static List<Account> getAllAccounts(){
        List<Account> accounts = new ArrayList<>();
        try {
            String sql = "SELECT * FROM account";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next())
                accounts.add(
                    new Account(rs.getInt("account_id"), rs.getDouble("balance"), rs.getInt("account_user"))
                );
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return accounts;
    }

    public static Account updateAccount(Account account){
        try {
            String sql = "UPDATE account SET amount = ? WHERE account_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, account.getBalance());
            preparedStatement.setInt(2, account.getAccount_id());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return account;
    }

    public static List<Account> getAccountsByUserID(int account_user){

        List<Account> accounts = new ArrayList<>();
        try {
            String sql = "SELECT * FROM account WHERE account_user = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, account_user);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next())
                accounts.add(
                    new Account(rs.getInt("account_id"), rs.getDouble("balance"), rs.getInt("account_user"))
                );
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return accounts;
    }
}
