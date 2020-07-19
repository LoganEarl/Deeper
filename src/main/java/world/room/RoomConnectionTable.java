package main.java.world.room;

import main.java.database.DatabaseManager;

import java.util.*;

public class RoomConnectionTable implements DatabaseManager.DatabaseTable{
    public static final String TABLE_NAME = "roomConnection";
    /**The unique id of the connection*/
    public static final String CONNECTION_ID = "connection";
    /**The name of the connection as displayed to the user.*/
    public static final String NAME = "name";
    /**The message displayed to the user after traversing the connection. Certain keywords can be used in the message to replace them with in game names. The keywords are as follows
     * <br>ENTITY1_NAME is replace with the name of the first entity
     * <br>ENTITY1_PRONOUN is replaced with the entity's pronoun. (it, he, she, they, etc)
     * <br>ENTITY1_REFLEXIVE is replaced with the reflexive form of the entity's pronoun. (itself, himself, herself, theirself, etc
     * <br>ENTITY1_POSSESSIVE is replaced with the possesive form of the entity's pronoun. (its, his, her, their)*/
    public static final String SUCCESS_MESSAGE = "successMessage";
    /**The message displayed to the user after failing the traversal. Ignored if no traverseSkillName specified*/
    public static final String FAILURE_MESSAGE = "failureMessage";
    /**The room players start in when traversing*/
    public static final String SOURCE_ROOM_NAME = "sourceRoomName";
    /**The room players end up in after successfully traversing connection*/
    public static final String DEST_ROOM_NAME = "destRoomName";
    /**The domains the user must be in to go through connection. Must be semicolon-separated list. Leave blank to assume all domains*/
    public static final String SOURCE_DOMAINS = "sourceDomains";
    /**Semicolon separated list of domains the user can enter the destination in. If no destDomain matches the domain user is currently in or none specified, they are placed in first available domain.*/
    public static final String DESTINATION_DOMAINS = "destinationDomains";
    /**The difficulty of the skill check to traverse the connection. Is a signed int, is added to a skill check roll. 0 is normal, 20 is pretty easy, -20 pretty hard. 60 is trivial, -60 hellish*/
    public static final String TRAVERSE_DIFFICULTY = "traverseDifficulty";
    /**The skill required to traverse the room connection. If no value given, no skill check is needed.*/
    public static final String TRAVERSE_SKILL_NAME = "traverseSkillName";
    /**The difficulty of the hidden skill check to detect room connection. If no value no skill check is needed. If the hidden skill check fails, the player will have to either search the room via a look command, or re-enter the room to reroll the check. Checking this way will only work if enough time has elapsed*/
    public static final String DETECT_DIFFICULTY = "detectDifficulty";
    /**The domain the player must be in to have a chance at detecting the room connection. If no value is given, all domains are assumed valid*/
    public static final String DETECT_DOMAINS = "detectDomain";
    /**A special word that the player must say to reveal the room connection*/
    public static final String DETECT_WORD = "detectWord";
    /**The name of the effect the player is granted upon failing to enter the room*/
    public static final String FAILURE_EFFECT_NAME = "failureEffectName";
    /**The name of the room the player ends up in instead of the normal destination*/
    public static final String FAILURE_ROOM_NAME = "failureRoomName";
    /**A comma separated list of destination domains the player can end up in upon failing the traverse check. Useless if freely traversable. If no destDomain matches the domain user is currently in or none specified, they are place in first available domain.*/
    public static final String FAILURE_DESTINATION_DOMAINS = "failureDestinationDomains";
    /**The name of the status effect applied to the player upon successful traversal. Leave blank for no effect*/
    public static final String SUCCESS_EFFECT_NAME = "successEffectName";
    /**The amount the base damage scales based on degrees of success on a traverse attempt. Has no effect if connection is freely traversable. Blank values will be substituted with a 1*/
    public static final String STAMINA_COST = "staminaCost";
    /**The code of the key item required to unlock the passage. Must unlock the room with a key with the same keycode. If 0 or NULL it is not lockable via key*/
    public static final String KEY_CODE = "keyCode";
    /**The state of the room. Determines if it is locked, unlocked, impassable*/
    public static final String STATE = "state";
    /**The direction of the room connection. Used to help orient players*/
    public static final String DIRECTION = "direction";
    /**The direction the player will go on a failed skill check. Should be the direction of the FAILURE_ROOM_NAME. Does nothing if no failure room name is set*/
    public static final String FAILURE_DIRECTION = "failureDirection";
    /**The base cooldown to detect a room again. Can be affected by the perception of entity*/
    public static final String DETECT_COOLDOWN_SECONDS = "detectCooldownSeconds";
    /**The room id of a room connection whose entry point is this room's exit point, and whose exit point is this room's entry point*/
    public static final String LINKED_CONNECTION_ID = "linkedConnectionID";

    private final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();
    private final Set<String> CONSTRAINTS = new HashSet<>(2);

    public RoomConnectionTable(){
        TABLE_DEFINITION.put(CONNECTION_ID, "VARCHAR(32) PRIMARY KEY NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(NAME, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(SUCCESS_MESSAGE, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(FAILURE_MESSAGE, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(SOURCE_ROOM_NAME, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(DEST_ROOM_NAME, "VARCHAR(32) NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(SOURCE_DOMAINS, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(DESTINATION_DOMAINS, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(TRAVERSE_DIFFICULTY, "INT NOT NULL DEFAULT 0");
        TABLE_DEFINITION.put(TRAVERSE_SKILL_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(DETECT_DIFFICULTY, "INT");
        TABLE_DEFINITION.put(DETECT_DOMAINS, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(DETECT_WORD, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(FAILURE_EFFECT_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(FAILURE_ROOM_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(FAILURE_DESTINATION_DOMAINS, "TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(SUCCESS_EFFECT_NAME, "VARCHAR(32) COLLATE NOCASE");
        TABLE_DEFINITION.put(STAMINA_COST, "INT NOT NULL DEFAULT 10");
        TABLE_DEFINITION.put(KEY_CODE, "INT");
        TABLE_DEFINITION.put(STATE, "VARCHAR(16) COLLATE NOCASE");
        TABLE_DEFINITION.put(DIRECTION, "VARCHAR(16) COLLATE NOCASE NOT NULL DEFAULT north");
        TABLE_DEFINITION.put(FAILURE_DIRECTION, "VARCHAR(16) COLLATE NOCASE");
        TABLE_DEFINITION.put(DETECT_COOLDOWN_SECONDS, "INT DEFAULT 30");
        TABLE_DEFINITION.put(LINKED_CONNECTION_ID, "VARCHAR(32)");

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
