package world.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Race {
    //TODO i copied the desc from dnd so make sure to change that later
    public static final Race HUMAN = new Race(
            "Human",
            "human",
            "Humans are the most adaptable and ambitious people among the common races. They have widely varying tastes, morals, and customs in the many different lands where they have settled. When they settle, though, they stay: they build cities to last for the ages, and great kingdoms that can persist for long centuries. An individual human might have a relatively short life span, but a human nation or culture preserves traditions with origins far beyond the reach of any single human’s memory. They live fully in the present—making them well suited to the adventuring life—but also plan for the future, striving to leave a lasting legacy. Individually and as a group, humans are adaptable opportunists, and they stay alert to changing political and social dynamics.",
            25,
            25,
            25,
            25
    );

    private static final String INSERT_SQL = String.format(Locale.US, "REPLACE INTO %s(%s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)", RaceTable.TABLE_NAME, RaceTable.IDENTIFIER, RaceTable.DISPLAY_NAME, RaceTable.DESCRIPTION, RaceTable.BASE_INT, RaceTable.BASE_WIS, RaceTable.BASE_STR, RaceTable.BASE_DEX);


    private String displayName;
    private String databaseName;
    private String description;
    private int baseStr;
    private int baseDex;
    private int baseInt;
    private int baseWis;

    Race(String displayName, String databaseName, String description, int baseStr, int baseDex, int baseInt, int baseWis){
        this.displayName = displayName;
        this.databaseName = databaseName;
        this.description = description;
        this.baseDex = baseDex;
        this.baseInt = baseInt;
        this.baseStr = baseStr;
        this.baseWis = baseWis;
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

    public static Race getFromDatabaseRace(String databaseName){
        for(Race r: defaultRaces())
            if(r.getDatabaseName().equals(databaseName))
                return r;
        return null;
    }

    public static void writePlayableRacesToDatabaseFile(String databaseName){

    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * gets the name of the race as it would be stored in the database.
     * @return the string value of the race as it would be stored in the database.
     */
    public String getDatabaseName() {
        return databaseName;
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
}
