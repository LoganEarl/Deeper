package world.item.weapon;

import database.DatabaseManager;
import world.item.ItemStatTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class WeaponStatTable implements  DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "weaponStat";

    public static final String ITEM_NAME = ItemStatTable.ITEM_NAME;
    public static final String MIN_BASE_DAMAGE = "minBaseDmg";
    public static final String MAX_BASE_DAMAGE = "maxBaseDmg";
    public static final String STR_SCALAR = "strScalar";
    public static final String DEX_SCALAR = "dexScalar";
    public static final String INT_SCALAR = "intScalar";
    public static final String WIS_SCALAR = "wisScalar";

    public static final String HIT_BONUS = "hitBonus";
    public static final String DAMAGE_TYPE = "damageType";

    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", TABLE_NAME, ITEM_NAME);

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    private static final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    private static final Set<String> CONSTRAINTS = new HashSet<>(1);

    public WeaponStatTable(){
        TABLE_DEFINITION.put(ITEM_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(MIN_BASE_DAMAGE, "INT");
        TABLE_DEFINITION.put(MAX_BASE_DAMAGE, "INT");
        TABLE_DEFINITION.put(STR_SCALAR, "DECIMAL");
        TABLE_DEFINITION.put(DEX_SCALAR, "DECIMAL");
        TABLE_DEFINITION.put(INT_SCALAR, "DECIMAL");
        TABLE_DEFINITION.put(WIS_SCALAR, "DECIMAL");

        TABLE_DEFINITION.put(HIT_BONUS, "INT");
        TABLE_DEFINITION.put(DAMAGE_TYPE, "VARCHAR(16)");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ITEM_NAME, ItemStatTable.TABLE_NAME, ItemStatTable.ITEM_NAME));
    }

    public static Map<String,String> getStatsForWeapon(String itemName, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Map<String, String> itemStats;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,itemName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next()) {
                    itemStats = ItemStatTable.getStatsFromResultSet(accountSet, TABLE_DEFINITION);
                }else
                    itemStats = null;
                getSQL.close();
            }catch (SQLException e){
                itemStats = null;
            }
        }
        return itemStats;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Map<String, String> getColumnDefinitions() {
        return TABLE_DEFINITION;
    }

    @Override
    public Set<String> getConstraints() {
        return CONSTRAINTS;
    }
}
