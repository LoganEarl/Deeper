package client;

import database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * This class is used to persist and store account information to the database. An "account"
 * is independent of a client, as an account can be accessed from different clients
 */
public class Account implements DatabaseManager.DatabaseEntry {
    private static final String DB_NAME = "account.db";

    private String userName;
    private String newUserName;
    private String hashedPassword;
    private String email;
    private AccountType accountType;

    private static final String GET_ACCOUNT_SQL = "SELECT * FROM " +
            AccountTable.TABLE_NAME + " WHERE " +
            AccountTable.USER_NAME + "=?";
    private static final String UPDATE_ACCOUNT_SQL = String.format(Locale.US,
            "UPDATE %s SET %s=?, %s=?, %s=?, %s=? WHERE %s=?",
            AccountTable.TABLE_NAME, AccountTable.USER_NAME, AccountTable.HASHED_PASSWORD,
            AccountTable.EMAIL, AccountTable.ACCOUNT_TYPE, AccountTable.USER_NAME);

    private static final String CREATE_ACCOUNT_SQL = String.format(Locale.US,
            "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
            AccountTable.TABLE_NAME,AccountTable.USER_NAME, AccountTable.HASHED_PASSWORD,
            AccountTable.EMAIL, AccountTable.ACCOUNT_TYPE);
    private static final String DELETE_ACCOUNT_SQL = String.format(Locale.US,
            "DELETE FROM %s WHERE %s=?", AccountTable.TABLE_NAME, AccountTable.USER_NAME);


    private Account(ResultSet readEntry) throws SQLException{
        this.userName = readEntry.getString(AccountTable.USER_NAME);
        this.hashedPassword = readEntry.getString(AccountTable.HASHED_PASSWORD);
        this.email = readEntry.getString(AccountTable.EMAIL);
        this.accountType = AccountType.fromInt(readEntry.getInt(AccountTable.ACCOUNT_TYPE));
        newUserName = userName;
    }

    /**
     * constructor for accounts that did not provide an email
     * @param userName the username of the account
     * @param hashedPassword the hashed form of the password to store and use to authenticate
     * @param type the level/privileges of the account to create
     */
    public Account(String userName, String hashedPassword, AccountType type){
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.accountType = type;
        this.email = "";
        newUserName = userName;
    }

    /**
     * constructor for accounts including an email.
     * @param userName the username of the account
     * @param hashedPassword the hashed form of the password to store and use to authenticate
     * @param email the email address of the client. Form www.*@*
     * @param type the level/privileges of the account to create
     */
    public Account(String userName, String hashedPassword, String email, AccountType type){
        this(userName, hashedPassword, type);
        this.email = email;
    }

    /**
     * will attempt to get the account with the given username from the given database
     * @param userName the userName of the account
     * @param databaseName the name of the database file containing the account
     * @return the Account object or null if failed to find account or some sort of exception
     */
    public static Account getAccountByUsername(String userName, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        Account toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_ACCOUNT_SQL);
                getSQL.setString(1,userName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new Account(accountSet);
                else
                    toReturn = null;
                getSQL.close();
                //c.close();
            }catch (SQLException e){
                toReturn = null;
            }
        }
        return toReturn;
    }

    /**
     * checks the given password against stored password and gets if they match
     * @param hashedPassword the password to check
     * @return true if the given password matches the stored one
     */
    public boolean checkPassword(String hashedPassword){
        return hashedPassword.equals(this.hashedPassword);
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        Account account = getAccountByUsername(this.userName,databaseName);
        if(account == null){
            return DatabaseManager.executeStatement(CREATE_ACCOUNT_SQL,databaseName,
                    userName,hashedPassword,email,accountType.getSavableForm()) > 0;
        }else{
            return updateInDatabase(databaseName);
        }
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        return DatabaseManager.executeStatement(DELETE_ACCOUNT_SQL,databaseName, newUserName) > 0;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        return DatabaseManager.executeStatement(UPDATE_ACCOUNT_SQL,databaseName,
                newUserName,hashedPassword,email,accountType.getSavableForm(),userName) > 0;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        Account account = getAccountByUsername(this.userName,databaseName);
        return account != null;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        newUserName = userName;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(obj instanceof Account)
            return ((Account)obj).userName.equals(userName);
        return false;
    }

    public enum AccountType {
        BASIC(1), MODERATOR(2), ADMIN(3), GOD(5);

        private int typeRep;
        AccountType(int typeRep){
            this.typeRep = typeRep;
        }

        public static AccountType fromInt(int i){
            for(AccountType t: AccountType.values()){
                if(t.typeRep == i)
                    return t;
            }
            return BASIC;
        }

        public int getSavableForm(){
            return typeRep;
        }

        public int compareToAcountType(AccountType that){
            return this.typeRep - that.typeRep;
        }
    }
}
