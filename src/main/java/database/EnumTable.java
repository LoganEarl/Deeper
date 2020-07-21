package main.java.database;

import java.sql.ResultSet;
import java.util.*;

public class EnumTable<T extends Enum<T>> implements DatabaseManager.DatabaseTable {
    public static final String ENUM_NAME = "name";

    private final String insertSql;

    private String tableName;
    private Class<T> enumClass;
    private Map<String,String> tableDefinition = new HashMap<>();

    public EnumTable(String tableName, Class<T> enumClass){
        this.tableName = tableName;
        this.enumClass = enumClass;

        insertSql = String.format(Locale.US, "REPLACE INTO %s(%s) VALUES(?)", tableName, ENUM_NAME);

        tableDefinition.put(ENUM_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL");

    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return tableDefinition;
    }

    @Override
    public Set<String> getConstraints() {
        return Collections.emptySet();
    }

    public void writeConstantsToDatabaseFile(String databaseName){
        for(T type: enumClass.getEnumConstants())
            DatabaseManager.executeStatement(insertSql ,databaseName, type.name());
    }

    public static <E extends Enum<E>> E extractFromResultSet(ResultSet readFrom, E defaultValue, Class<E> classValue) {
        try{
            return E.valueOf(classValue, readFrom.getString(ENUM_NAME));
        }catch (Exception e){
            System.out.println("An enum had an un-parsable type and defaulted to " + defaultValue);
            return defaultValue;
        }
    }
}


