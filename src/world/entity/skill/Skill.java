package world.entity.skill;

public enum Skill {
    recover1("recover1","RecoverI",250),
    recover2("recover2","RecoverII", 1000),
    recover3("recover3","RecoverIII", 4000),
    recover4("recover4","RecoverIV",8000),
    recover5("recover5","RecoverV", 16000);

    private String savableName;
    private String displayName;
    private int xpCost;

    Skill(String savableName, String displayName, int xpCost){
        this.displayName = displayName;
        this.savableName = savableName;
        this.xpCost = xpCost;
    }

    public String getSavableName() {
        return savableName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getXpCost() {
        return xpCost;
    }

    public static Skill getSkillOfDisplayName(String displayName){
        for(Skill skill: Skill.values()){
            if(skill.displayName.toLowerCase().equals(displayName.toLowerCase()))
                return skill;
        }
        return null;
    }

    public static Skill getSkillOfDatabaseName(String databaseName) {
        for (Skill skill : Skill.values()) {
            if (skill.savableName.toLowerCase().equals(databaseName.toLowerCase()))
                return skill;
        }
        return null;
    }
}
