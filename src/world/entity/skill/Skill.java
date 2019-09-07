package world.entity.skill;

import com.sun.istack.internal.Nullable;
import world.entity.Entity;
import world.entity.StatContainer;

import static world.entity.skill.Skill.VisibilityType.*;

public enum Skill {
    //Stabilize===========================
    stabilize1("stabilize1", "Stabilize", 500,
            new StatContainer(0, 0, 0, 10, 0, 0), visible),
    stabilize2("stabilize2", "Improved Stabilize", 500,
            new StatContainer(0, 0, 0, 15, 0, 0), hiddenUntilSkills,
            stabilize1),
    stabilize3("stabilize3", "Refined Stabilize", 1500,
            new StatContainer(0, 0, 0, 25, 0, 0), hiddenUntilSkills,
            stabilize2),

    //Recover===========================
    recover1("recover1", "Recover", 250,
            new StatContainer(0, 0, 0, 0, 15, 10), visible),
    recover2("recover2", "Improved Recover", 1000,
            new StatContainer(0, 0, 0, 0, 30, 20), hiddenUntilSkills,
            recover1),
    recover3("recover3", "Unnatural Recover", 4000,
            new StatContainer(0, 0, 0, 0, 60, 40), hiddenUntilLearnable,
            recover2),
    recover4("recover4", "Unreal Recover", 12000,
            new StatContainer(0, 0, 0, 0, 120, 80), hiddenUntilLearnable,
            recover3);

    private String savableName;
    private String displayName;
    private int xpCost;
    private Skill[] requiredSkills;
    private StatContainer requiredStats;
    private VisibilityType visibilityType;

    Skill(String savableName, String displayName, int xpCost, @Nullable StatContainer requiredStats, VisibilityType visibilityType, Skill... requiredSkills) {
        this.displayName = displayName;
        this.savableName = savableName;
        this.xpCost = xpCost;
        this.requiredSkills = requiredSkills;
        this.requiredStats = requiredStats;
        this.visibilityType = visibilityType;
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

    public StatContainer getRequiredStats() {
        return requiredStats;
    }

    public Skill[] getRequiredSkills() {
        return requiredSkills;
    }

    public static Skill getSkillOfDisplayName(String displayName) {
        for (Skill skill : Skill.values()) {
            if (skill.displayName.toLowerCase().equals(displayName.toLowerCase()))
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

    public boolean isVisibleToEntity(Entity entity) {
        switch (visibilityType) {
            case visible:
                return true;
            case hiddenUntilSkills:
                for (Skill prereq : requiredSkills)
                    if (!SkillTable.entityHasSkill(entity, prereq)) return false;
                return true;
            case hiddenUntilLearnable:
                for (Skill prereq : requiredSkills)
                    if (!SkillTable.entityHasSkill(entity, prereq)) return false;
            case hiddenUntilStats:
                StatContainer stats = entity.getStats();
                return stats.getStrength() >= requiredStats.getStrength() &&
                        stats.getDexterity() >= requiredStats.getDexterity() &&
                        stats.getIntelligence() >= requiredStats.getIntelligence() &&
                        stats.getWisdom() >= requiredStats.getWisdom() &&
                        stats.getFitness() >= requiredStats.getFitness() &&
                        stats.getToughness() >= requiredStats.getToughness();
            case hidden:
                return false;
        }
        return false;
    }

    public enum VisibilityType {
        visible, hiddenUntilSkills, hiddenUntilStats, hiddenUntilLearnable, hidden;
    }
}
