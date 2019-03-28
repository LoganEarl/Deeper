package world.item;

import database.DatabaseManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the stats of different containers. Distinct from an instance of a container in that it has no location,
 * bound items, etc. This table just stores the stats so that the data usage is minimized.<br>
 *     Note, when it comes to maximum storage potential, any value (kg,l,#) can be null, and all not null values are applied
 * @author Logan Earl
 */
public class ContainerStatTable implements DatabaseManager.DatabaseTable {
    /**The name of the table used to store container stats*/
    public static final String TABLE_NAME = "containerStats";

    /**The name of the container*/
    public static final String CONTAINER_NAME = "containerName";
    /**The description of the container*/
    public static final String CONTAINER_DESCRIPTION = "containerDescription";
    /**The maximum storage potential of the container in KGs*/
    public static final String MAX_KGS = "maxKgs";
    /**The maximum storage potential of the container in Liters*/
    public static final String MAX_LITERS = "maxLiters";
    /**The maximum storage potential of the container in number of items stored*/
    public static final String MAX_NUMBER = "maxItems";
    /**Stores how difficult the lock is to pick. 0 means it has no lock*/
    public static final String LOCK_DIFFICULTY = "lockDifficulty";

    /**A Map, containing the column names as keys and the associated data-type of the column as values*/
    public final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    public ContainerStatTable(){
        TABLE_DEFINITION.put(CONTAINER_NAME, "VARCHAR(32) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(CONTAINER_DESCRIPTION, "TEXT");
        TABLE_DEFINITION.put(MAX_KGS,"DECIMAL");
        TABLE_DEFINITION.put(MAX_LITERS,"DECIMAL");
        TABLE_DEFINITION.put(MAX_NUMBER,"INT");
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
    public List<String> getConstraints() {
        return null;
    }
}
