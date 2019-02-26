package clientManagement;

import databaseUtils.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

public class AccountTable implements DatabaseManager.DatabaseTable {

    public static final String TABLE_NAME = "account";
    public final Map<String, String> TABLE_DEFINITION = new HashMap<>();

    public AccountTable(){
        TABLE_DEFINITION.put("userId","INT PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put("userName","VARCHAR(16) NOT NULL");
        TABLE_DEFINITION.put("hashedPassword","VARCHAR(20) NOT NULL");
        TABLE_DEFINITION.put("email","TEXT");
        TABLE_DEFINITION.put("accountType","INT NOT NULL");
    }

    public String getTableName(){
        return TABLE_NAME;
    }

    public static Account getAccountByUsername(String userName, String databseName){
        //TODO
        return null;
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
