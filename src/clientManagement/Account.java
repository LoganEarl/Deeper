package clientManagement;

import databaseUtils.DatabaseManager;

/**
 * This class is used to persist and store account information to the database. An "account"
 * is independent of a client, as an account can be accessed from different clients
 */
public class Account implements DatabaseManager.DatabaseEntry {
    private String userName;
    private String hashedPassword;
    private String email;

    /**
     * sole constructor
     * @param userName the username of the account
     * @param hashedPassword the hashed form of the password to store and use to authenticate
     * @param type the level/privileges of the account to create
     */
    public Account(String userName, String hashedPassword, AccountTable.AccountType type){
        //TODO

    }

    public boolean checkPassword(String hashedPassword){
        return hashedPassword.equals(this.hashedPassword);
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        return false;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return false;
    }
}
