package main.java.world.room;

import main.java.database.DatabaseManager;

import java.util.*;

public class RoomConnectionTable implements DatabaseManager.DatabaseTable{
    public static final String TABLE_NAME = "roomConnection";
    /**The unique id of the connection*/
    public static final String CONNECTION_ID = "connection";
    /**The name of the connection as displayed to the user*/
    public static final String NAME = "name";
    /**The message displayed to the user after traversing the connection*/
    public static final String SUCCESS_MESSAGE = "successMessage";
    /**The message displayed to the user after failing the traversal. Ignored if no traverseSkillName specified*/
    public static final String FAILURE_TEXT = "failureMessage";
    /**The room players start in when traversing*/
    public static final String SOURCE_ROOM_NAME = "sourceRoomName";
    /**The room players end up in after successfully traversing connection*/
    public static final String DEST_ROOM_NAME = "destRoomName";
    /**The domains the user must be in to go through connection. Must be semicolon-separated list. Leave blank to assume all domains*/
    public static final String SOURCE_DOMAINS = "sourceDomains";
    /**Semicolon seperated list of domains the user can enter the destination in. If no destDomain matches the domain user is currently in or none specified, they are placed in first available domain.*/
    public static final String DESTINATION_DOMAINS = "destinationDomains";
    /**The difficulty of the skill check to traverse the connection. Is a positive int, user must roll higher than this int in a skill check. If no value given, no skill check is needed*/
    public static final String TRAVERSE_DIFFICULTY = "traverseDifficulty";
    /**The skill required to traverse the room connection. If no value given, no skill check is needed.*/
    public static final String TRAVERSE_SKILL_NAME = "traverseSkillName";
    /**The difficulty of the hidden skill check to detect room connection. If no value is given, no skill check is needed. If the hidden skill check fails, the player will have to either
     * search the room via a look command, or re-enter the room to reroll the check*/
    public static final String DETECT_DIFFICULTY = "detectDifficulty";
    /**The domain the player must be in to have a chance at detecting the room connection. If no value is given, all domains are assumed valid*/
    public static final String DETECT_DOMAINS = "detectDomain";
    /**A special word that the player must say to reveal the room connection*/
    public static final String DETECT_WORD = "detectWord";
    /**The name of the effect the player is granted upon failing to enter the room*/
    public static final String FAILURE_EFFECT_NAME = "failureEffectName";
    /**The amount the base damage scales based on degrees of failure on a traverse attempt. Has no effect if connection is freely traversable. Blank values will be substituted with a 1*/
    public static final String FAILURE_DAMAGE_SCALAR = "failureDamageScalar";
    /**The base damage dealt to the player upon failing to traverse the connection. If negative, will heal player.*/
    public static final String FAILURE_BASE_DAMAGE = "failureBaseDamage";
    /**The name of the room the player ends up in instead of the normal destination*/
    public static final String FAILURE_ROOM_NAME = "failureRoomName";
    /**A comma separated list of destination domains the player can end up in upon failing the traverse check. Useless if freely traversable. If no destDomain matches the domain user is currently in or none specified, they are place in first available domain.*/
    public static final String FAILURE_DESTINATION_DOMAINS = "failureDestinationDomains";
    /**The name of the status effect applied to the player upon successful traversal. Leave blank for no effect*/
    public static final String SUCCESS_EFFECT_NAME = "successEffectName";
    /**The amount the base damage scales based on degrees of success on a traverse attempt. Has no effect if connection is freely traversable. Blank values will be substituted with a 1*/
    public static final String SUCCESS_DAMAGE_SCALAR = "successDamageScalar";
    /**The base damage dealt to the player upon succeeding to traverse the connection. If negative, will heal player.*/
    public static final String SUCCESS_BASE_DAMAGE = "successBaseDamage";
    /**The stamina cost to traverse the connection*/
    public static final String STAMINA_COST = "staminaCost";
    /**The code of the key item required to unlock the passage. Must traverse using the */
    public static final String KEY_CODE = "keyCode";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final Set<String> CONSTRAINTS = new HashSet<>(2);

    public RoomConnectionTable(){
        TABLE_DEFINITION.put(CONNECTION_ID, "VARCHAR(32) PRIMARY KEY NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(NAME, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(SUCCESS_MESSAGE, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(FAILURE_TEXT, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(SOURCE_ROOM_NAME, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(DEST_ROOM_NAME, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(SOURCE_DOMAINS, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(DESTINATION_DOMAINS, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(TRAVERSE_DIFFICULTY, "INT");
        TABLE_DEFINITION.put(TRAVERSE_SKILL_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(DETECT_DIFFICULTY, "INT");
        TABLE_DEFINITION.put(DETECT_DOMAINS, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(DETECT_WORD, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(FAILURE_EFFECT_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(FAILURE_DAMAGE_SCALAR, "DECIMAL");
        TABLE_DEFINITION.put(FAILURE_BASE_DAMAGE, "INT");
        TABLE_DEFINITION.put(FAILURE_ROOM_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(FAILURE_DESTINATION_DOMAINS, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(SUCCESS_EFFECT_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(SUCCESS_DAMAGE_SCALAR, "DECIMAL");
        TABLE_DEFINITION.put(SUCCESS_BASE_DAMAGE, "INT");
        TABLE_DEFINITION.put(STAMINA_COST, "INT");
        TABLE_DEFINITION.put(KEY_CODE, "INT");

        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                SOURCE_ROOM_NAME, RoomTable.TABLE_NAME, RoomTable.ROOM_NAME));
        CONSTRAINTS.add(String.format(Locale.US,"FOREIGN KEY (%s) REFERENCES %s(%s)",
                DEST_ROOM_NAME, RoomTable.TABLE_NAME, RoomTable.ROOM_NAME));

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
