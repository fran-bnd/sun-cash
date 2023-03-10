package Model;

import java.time.LocalDateTime;

public class Transaction {

    private int TransactionId;

    //    private String transactionType;
    private double amount;

    private LocalDateTime transactionTime;
    private int accountUser;
    private int accountID;

    private String transactionType;

    public Transaction(){

    }

    public Transaction( String transactionType, double amount, LocalDateTime transactionTime,
                       int accountUser, int accountID) {

        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.accountUser = accountUser;
        this.accountID = accountID;

    }

    public Transaction(int transactionId, String transactionType, double amount, LocalDateTime transactionTime,
                       int accountUser, int accountID) {
        TransactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.accountUser = accountUser;
        this.accountID = accountID;

    }

    public int getTransactionId() {
        return TransactionId;
    }

    public void setTransactionId(int transactionId) {
        TransactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public int getAccountUser() {
        return accountUser;
    }

    public void setAccountUser(int accountUser) {
        this.accountUser = accountUser;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }
}


