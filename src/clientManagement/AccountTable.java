package clientManagement;

import databaseUtils.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds information relating to the creation of a SQL table that holds each account that has been created.
 * Also can create and retrieve accounts from a given database file.
 * @author Logan Earl
 */
public class AccountTable implements DatabaseManager.DatabaseTable {
    /**
     * the name of the table used to store accounts
     */
    public static final String TABLE_NAME = "account";
    public static final String USER_NAME = "userName";
    public static final String HASHED_PASSWORD = "hashedPassword";
    public static final String EMAIL = "email";
    public static final String ACCOUNT_TYPE = "accountType";
    /**
     * A Map, containing the column names as keys and the associated data-type of the column as values
     */
    public final Map<String, String> TABLE_DEFINITION = new HashMap<>();

    public AccountTable(){
        TABLE_DEFINITION.put(USER_NAME,"VARCHAR(16) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(HASHED_PASSWORD,"VARCHAR(20) NOT NULL");
        TABLE_DEFINITION.put(EMAIL,"TEXT");
        TABLE_DEFINITION.put(ACCOUNT_TYPE,"INT NOT NULL");
    }

    public String getTableName(){
        return TABLE_NAME;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return TABLE_DEFINITION;
    }

    public enum AccountType{
        BASIC(-1), MODERATOR(-2), ADMIN(-3), GOD(-5);

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
    }
}
