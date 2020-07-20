package main.java.world.entity.race;

import main.java.database.DatabaseManager;
import main.java.world.trait.Trait;
import main.java.world.trait.TraitBestower;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * instantiated from of a race. Note, all playable races are stored as constants of this class. When a new world is instantiated, these playable races need to be replaced into the new file to ensure that players migrating to the main.java.world do not fail their foreign key restraints.
 * @author Logan Earl
 */
public class Race implements TraitBestower {
    public static final Race HUMAN = new Race(
            "Human",
            "human",
            "An adaptable and ambitious race capable of almost anything given time and motivation",
            10,
            10,
            10,
            10,
            10,
            10, Trait.testPools
    );

    private static final String INSERT_SQL = String.format(Locale.US, "REPLACE INTO %s(%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", RaceTable.TABLE_NAME, RaceTable.RACE_ID, RaceTable.DISPLAY_NAME, RaceTable.DESCRIPTION, RaceTable.BASE_INT, RaceTable.BASE_WIS, RaceTable.BASE_STR, RaceTable.BASE_DEX, RaceTable.BASE_TOUGH, RaceTable.BASE_FIT);
    private static final String GET_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s=?", RaceTable.TABLE_NAME, RaceTable.RACE_ID);

    private static final Map<String,Race> unplayableRaceCache = new HashMap<>();

    private String displayName;
    private String identifier;
    private String description;
    private String databaseName;
    private int baseStr;
    private int baseDex;
    private int baseInt;
    private int baseWis;
    private int baseTough;
    private int baseFit;
    private Set<Trait> inherentTraits;

    private Race(String displayName, String identifier, String description, int baseStr, int baseDex, int baseInt, int baseWis, int baseTough, int baseFit, Trait... bestowedTraits){
        this.displayName = displayName;
        this.identifier = identifier;
        this.description = description;
        this.baseDex = baseDex;
        this.baseInt = baseInt;
        this.baseStr = baseStr;
        this.baseWis = baseWis;
        this.baseTough = baseTough;
        this.baseFit = baseFit;
        inherentTraits = new HashSet<>();
        inherentTraits.addAll(Arrays.asList(bestowedTraits));
    }

    private Race(ResultSet entry, String databaseName) throws SQLException {
        displayName = entry.getString(RaceTable.DISPLAY_NAME);
        identifier = entry.getString(RaceTable.RACE_ID);
        description = entry.getString(RaceTable.DESCRIPTION);
        baseDex = entry.getInt(RaceTable.BASE_DEX);
        baseInt = entry.getInt(RaceTable.BASE_INT);
        baseStr = entry.getInt(RaceTable.BASE_STR);
        baseWis = entry.getInt(RaceTable.BASE_WIS);
        baseTough = entry.getInt(RaceTable.BASE_TOUGH);
        baseFit = entry.getInt(RaceTable.BASE_FIT);
        String[] rawTraits = entry.getString(RaceTable.TRAITS).split(",");
        if(rawTraits.length == 1 && rawTraits[0].isEmpty()) rawTraits = new String[0];
        inherentTraits = new HashSet<>();
        for(String rawTrait: rawTraits){
            Trait t = Trait.getTraitFromSavable(rawTrait);
            if(t != null)
                inherentTraits.add(t);
        }

        this.databaseName = databaseName;
    }

    public Set<Trait> getBestowedTraits(){
        return inherentTraits;
    }

    public static List<Race> defaultRaces(){
        List<Race> defaultRaces = new ArrayList<>();
        Field[] fields = Race.class.getFields();
        for(Field f: fields){
            if(f.getType().equals(Race.class) && Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {
                try {
                   defaultRaces.add((Race) f.get(null));
                }catch (IllegalAccessException ignored){}
            }
        }
        return defaultRaces;
    }

    public static Race getFromID(String raceName, String databaseName){
        for(Race r: defaultRaces())
            if(r.getRaceID().equals(raceName))
                return r;
        return getUnplayableRaceByID(raceName,databaseName);
    }

    private static Race getUnplayableRaceByID(String id, String databaseName){
        final String tag = id + "<!SUB!>" + databaseName;
        if(unplayableRaceCache.containsKey(tag))
            return unplayableRaceCache.get(tag);

        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL = null;
        Race newRace;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,id);
                ResultSet raceEntries = getSQL.executeQuery();
                if(raceEntries.next())
                    newRace = new Race(raceEntries,databaseName);
                else
                    newRace = null;
                getSQL.close();
                //c.close();
            }catch (Exception e){
                newRace = null;
            }
        }

        if(newRace != null && !unplayableRaceCache.containsKey(tag))
            unplayableRaceCache.put(tag,newRace);

        return newRace;
    }

    public static void writePlayableRacesToDatabaseFile(String databaseName){
        for(Race r: defaultRaces()){
            DatabaseManager.executeStatement(INSERT_SQL,databaseName,
                    r.getRaceID(), r.getDisplayName(), r.getDescription(),
                    r.getBaseInt(), r.getBaseWis(), r.getBaseStr(), r.getBaseDex(), r.getBaseTough(),r.getBaseFit());
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * gets the name of the race as it would be stored in the database.
     * @return the string value of the race as it would be stored in the database.
     */
    public String getRaceID() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public int getBaseStr() {
        return baseStr;
    }

    public int getBaseDex() {
        return baseDex;
    }

    public int getBaseInt() {
        return baseInt;
    }

    public int getBaseWis() {
        return baseWis;
    }

    public int getBaseTough() {
        return baseTough;
    }

    public int getBaseFit() {
        return baseFit;
    }

    public String getDatabaseName(){
        return databaseName;
    }

    public static String getPlayableRaceDescriptions(){
        StringBuilder description = new StringBuilder();
        for(Race r: Race.defaultRaces())
            description.append(r.toString()).append("\n\n");
        return description.toString();
    }

    @Override
    public String toString(){
        return String.format(Locale.US,"%s: %s\nBase Stats: [STR %d] [DEX %d] [INT %d] [WIS %d] [TOUGH %d] [FIT %d]",
                displayName, description, baseStr, baseDex, baseInt, baseWis, baseTough, baseFit);
    }
}
