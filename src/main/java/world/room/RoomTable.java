package main.java.world.room;

import main.java.database.DatabaseManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds information relating to the creation of a SQL table that holds each room that has been created.
 * @author Logan Earl
 */
public class RoomTable implements DatabaseManager.DatabaseTable {
    public static final String TABLE_NAME = "room";

    /**The name of the room*/
    public static final String ROOM_NAME = "roomName";
    /**The extended description of the room*/
    public static final String ROOM_DESCRIPTION = "roomDescription";
    /**A semicolon separated list of valid domains present in this room. The first element is considered the default value for entities in room unless otherwise specified*/
    public static final String DOMAINS = "domains";

    //WIND
    /**Wind level is 0 to 100 scale with 0 being calm*/
    public static final String WIND_LEVEL = "windLevel";
    /**How windy the area is by default. 0 to 100 like the wind level*/
    public static final String WIND_EQUILIBRIUM = "windEquilibrium";
    /**How quickly the wind approaches equilibrium. Each tick the wind level will approach the equilibrium by WIND_CONDUCTIVITY * (EQUILIBRIUM - LEVEL). Value ranges from 0.001 to 1*/
    public static final String WIND_CONDUCTIVITY = "windConductivity";

    //RAIN
    /**Rain level from 0 to 100 with 0 being no rain. */
    public static final String RAIN_LEVEL = "rainLevel";
    public static final String RAIN_EQUILIBRIUM = "rainEquilibrium";
    public static final String RAIN_CONDUCTIVITY = "rainConductivity";

    //TEMP
    /**Temperature level. -100 to 100 with 0 being default. */
    public static final String TEMP_LEVEL = "tempLevel";
    public static final String TEMP_EQUILIBRIUM = "tempEquilibrium";
    public static final String TEMP_CONDUCTIVITY = "tempConductivity";

    //TOXIC
    /**Ranges from 0 to 100. 0 is no toxicity*/
    public static final String TOXIC_LEVEL = "toxicLevel";
    public static final String TOXIC_EQUILIBRIUM = "toxicEquilibrium";
    public static final String TOXIC_CONDUCTIVITY = "toxicConductivity";

    //ACID
    /**ranges from 0 to 100, 0 is no acidity*/
    public static final String ACID_LEVEL = "acidLevel";
    public static final String ACID_EQUILIBRIUM = "acidEquilibrium";
    public static final String ACID_CONDUCTIVITY = "acidConductivity";

    //ELECTRICITY
    /**Ranges from 0 to 100. 0 is no electricity. */
    public static final String ELECTRICITY_LEVEL = "electricityLevel";
    public static final String ELECTRICITY_EQUILIBRIUM = "electricityEquilibrium";
    public static final String ELECTRICITY_CONDUCTIVITY = "electricityConductivity";

    //INFO
    /**How dense reality is. Ranges from 0 to 100, where 100 is normal reality and 0 is an empty void*/
    public static final String INFO_LEVEL = "infoLevel";
    public static final String INFO_EQUILIBRIUM = "infoEquilibrium";
    public static final String INFO_CONDUCTIVITY = "infoConductivity";

    //QUAKE
    /**0 to 100, base value 0*/
    public static final String QUAKE_LEVEL = "quakeLevel";
    public static final String QUAKE_EQUILIBRIUM = "quakeEquilibrium";
    public static final String QUAKE_CONDUCTIVITY = "quakeConductivity";

    //OIL
    /**0 to 100, base value 0*/
    public static final String OIL_LEVEL = "oilLevel";
    public static final String OIL_EQUILIBRIUM = "oilEquilibrium";
    public static final String OIL_CONDUCTIVITY = "oilConductivity";

    public final Map<String, String> TABLE_DEFINITION = new LinkedHashMap<>();

    public RoomTable(){
        TABLE_DEFINITION.put(ROOM_NAME,"VARCHAR(32) PRIMARY KEY NOT NULL COLLATE NOCASE");
        TABLE_DEFINITION.put(ROOM_DESCRIPTION,"TEXT COLLATE NOCASE");
        TABLE_DEFINITION.put(DOMAINS,"TEXT COLLATE NOCASE");

        TABLE_DEFINITION.put(WIND_LEVEL,"NUMERIC");
        TABLE_DEFINITION.put(WIND_EQUILIBRIUM,"NUMERIC");
        TABLE_DEFINITION.put(WIND_CONDUCTIVITY,"NUMERIC");

        TABLE_DEFINITION.put(RAIN_LEVEL,"NUMERIC");
        TABLE_DEFINITION.put(RAIN_EQUILIBRIUM,"NUMERIC");
        TABLE_DEFINITION.put(RAIN_CONDUCTIVITY,"NUMERIC");

        TABLE_DEFINITION.put(TEMP_LEVEL,"NUMERIC");
        TABLE_DEFINITION.put(TEMP_EQUILIBRIUM,"NUMERIC");
        TABLE_DEFINITION.put(TEMP_CONDUCTIVITY,"NUMERIC");

        TABLE_DEFINITION.put(TOXIC_LEVEL,"NUMERIC");
        TABLE_DEFINITION.put(TOXIC_EQUILIBRIUM,"NUMERIC");
        TABLE_DEFINITION.put(TOXIC_CONDUCTIVITY,"NUMERIC");

        TABLE_DEFINITION.put(ACID_LEVEL,"NUMERIC");
        TABLE_DEFINITION.put(ACID_EQUILIBRIUM,"NUMERIC");
        TABLE_DEFINITION.put(ACID_CONDUCTIVITY,"NUMERIC");

        TABLE_DEFINITION.put(ELECTRICITY_LEVEL,"NUMERIC");
        TABLE_DEFINITION.put(ELECTRICITY_EQUILIBRIUM,"NUMERIC");
        TABLE_DEFINITION.put(ELECTRICITY_CONDUCTIVITY,"NUMERIC");

        TABLE_DEFINITION.put(INFO_LEVEL,"NUMERIC");
        TABLE_DEFINITION.put(INFO_EQUILIBRIUM,"NUMERIC");
        TABLE_DEFINITION.put(INFO_CONDUCTIVITY,"NUMERIC");

        TABLE_DEFINITION.put(QUAKE_LEVEL,"NUMERIC");
        TABLE_DEFINITION.put(QUAKE_EQUILIBRIUM,"NUMERIC");
        TABLE_DEFINITION.put(QUAKE_CONDUCTIVITY,"NUMERIC");

        TABLE_DEFINITION.put(OIL_LEVEL,"NUMERIC");
        TABLE_DEFINITION.put(OIL_EQUILIBRIUM,"NUMERIC");
        TABLE_DEFINITION.put(OIL_CONDUCTIVITY,"NUMERIC");
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
        return null;
    }
}
