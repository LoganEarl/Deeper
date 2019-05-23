package world.entity;

import database.DatabaseManager;
import world.room.RoomTable;

import java.util.*;

/**
 * Holds the database schema for a table containing all the information relevant to a player or npc entity.
 * @author Logan Earl
 */
public class EntityTable implements DatabaseManager.DatabaseTable {
    /**The name of the table used to store entity information*/
    public static final String TABLE_NAME = "entity";

    /**The string identifier of the entity. For players, this is their username. For monsters, this is some generic name like goblin83 and so forth*/
    public static final String ENTITY_ID = "entityID";
    /**The name displayed to players. If null, the entity id is used in its stead*/
    public static final String DISPLAY_NAME = "displayName";
    /**The current number of hit points possessed by the entity*/
    public static final String HP = "HP";
    /**the maximum allowed hp the entity can have*/
    public static final String MAX_HP = "maxHP";
    /**The amount of mind points possessed by the entity*/
    public static final String MP = "MP";
    /**The maximum amount of mind points the entity can have*/
    public static final String MAX_MP = "maxMP";
    /**The current amount of stamina points possessed by the entity*/
    public static final String STAMINA = "stamina";
    /**The maximum amount of stamina points the entity can have*/
    public static final String MAX_STAMINA = "maxStamina";
    /**The race of the entity. Foreign key to the race table*/
    public static final String RACE_ID = RaceTable.RACE_ID;

    /**The strength attribute of the entity. 10 is average*/
    public static final String STR = "strength";
    /**The dexterity attribute of the entity. 10 is average*/
    public static final String DEX = "dexterity";
    /**The intelligence attribute of the entity. 10 is average*/
    public static final String INT = "intelligence";
    /**The wisdom attribute of the entity. 10 is average*/
    public static final String WIS = "wisdom";

    /**The type of controller responsible for the entity. Example, {@value CONTROLLER_TYPE_PLAYER} or {@value CONTROLLER_TYPE_STATIC}*/
    public static final String CONTROLLER_TYPE = "controller";

    /**Used to denote that the entity is controlled by a player*/
    public static final String CONTROLLER_TYPE_PLAYER = "player";
    /**Used to denote that the entity is not controlled, and simply sits and does nothing*/
    public static final String CONTROLLER_TYPE_STATIC = "static";

    /**A foreign key to the room the entity is standing in*/
    public static final String ROOM_NAME = RoomTable.ROOM_NAME;

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final List<String> CONSTRAINTS = new ArrayList<>();

    public EntityTable(){
        TABLE_DEFINITION.put(ENTITY_ID, "VARCHAR(32) PRIMARY KEY NOT NULL");
        TABLE_DEFINITION.put(DISPLAY_NAME, "VARCHAR(32)");
        TABLE_DEFINITION.put(HP,"INT");
        TABLE_DEFINITION.put(MAX_HP,"INT");
        TABLE_DEFINITION.put(MP,"INT");
        TABLE_DEFINITION.put(MAX_MP,"INT");
        TABLE_DEFINITION.put(STAMINA,"INT");
        TABLE_DEFINITION.put(MAX_STAMINA,"INT");
        TABLE_DEFINITION.put(STR,"INT");
        TABLE_DEFINITION.put(DEX,"INT");
        TABLE_DEFINITION.put(INT,"INT");
        TABLE_DEFINITION.put(WIS,"INT");
        TABLE_DEFINITION.put(RACE_ID, "VARCHAR(16)");
        TABLE_DEFINITION.put(CONTROLLER_TYPE,"VARCHAR(16)");
        TABLE_DEFINITION.put(ROOM_NAME, "VARCHAR(32)");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                ROOM_NAME, RoomTable.TABLE_NAME, RoomTable.ROOM_NAME));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                RACE_ID, RaceTable.TABLE_NAME, RaceTable.RACE_ID));
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
    public List getConstraints() {
        return CONSTRAINTS;
    }
}
