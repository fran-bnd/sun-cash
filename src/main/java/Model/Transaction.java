package Model;

import java.sql.Timestamp;

public class Transaction {
    public enum TransactType { DEPOSIT("deposit", 0), WITHDRAWL("withdrawl", 1), TRANSFER("transfer", 2);
        private final String typeStr;
        private final int key;
        private TransactType(String s, int i) {
            typeStr = s;
            key = i;
        }
        public int key() {
            return key;
        }
        public static TransactType fromInt(int x) {
            final TransactType[] lookUp = {DEPOSIT, WITHDRAWL, TRANSFER};
            return lookUp[x];
        }
        @Override
        public String toString() {
            return typeStr;
        }
    }

    private int transactionID;
    private TransactType type;
    private long amount_cents;
    private Timestamp transactionTime;
    private int accountID_from;
    private int accountID_to;

    public Transaction() {
    }

    public Transaction(TransactType type, long amount, int from, int to) {
        this.type = type;
        amount_cents = amount;
        accountID_from = from;
        accountID_to = to;
    }

    public Transaction(TransactType type, long amount, Timestamp time, int from, int to) {
        this(type, amount, from, to);
        transactionTime = time;
    }

    public Transaction(int id, TransactType type, long amount, Timestamp time, int from, int to) {
        this(type, amount, time, from, to);
        transactionID = id;
    }

    public int getID() {
        return transactionID;
    }

    public void setID(int id) {
        transactionID = id;
    }

    public TransactType getType() {
        return type;
    }

    public void setType(TransactType type) {
        this.type = type;
    }

    public long getAmount() {
        return amount_cents;
    }

    public void setAmount(long amount) {
        amount_cents = amount;
    }

    public Timestamp getTime() {
        return transactionTime;
    }

    public void setTime(Timestamp transactionTime) {
        this.transactionTime = transactionTime;
    }

    public int getAccountFrom() {
        return accountID_from;
    }

    public void setAccountFrom(int accountID) {
        this.accountID_from = accountID;
    }

    public int getAccountTo() {
        return accountID_to;
    }

    public void setAccountTo(int accountID) {
        this.accountID_to = accountID;
    }

    @Override
    public String toString() {
        return "Transaction{transactionID=" + transactionID + ", type=" + type +
            ", amount_cents=" + amount_cents + ", transactionTime=" + transactionTime +
            ", accountID_from=" + accountID_from + ", accountID_to=" + accountID_to + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction transaction = (Transaction) o;
        return transactionID == transaction.transactionID && type == transaction.type &&
            amount_cents == transaction.amount_cents &&
            accountID_from == transaction.accountID_from &&
            accountID_to == transaction.accountID_to &&
            Math.abs(transactionTime.getTime() - transaction.transactionTime.getTime()) < 100;
    }
}
