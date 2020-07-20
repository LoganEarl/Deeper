package main.java.world.trait;
import main.java.world.entity.Attack;
import main.java.world.entity.StatContainer;
import main.java.world.entity.pool.EntityPoolContainer;
import main.java.world.entity.pool.PoolValueContainer;
import main.java.world.entity.skill.Skill;
import main.java.world.item.weapon.Weapon.DamageScalarContainer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Trait implements Attack.AttackDefenceModifier, Attack.AttackOffenceModifier {
    //Traversal
    flying("Capable of flight"),
    burrowing("Capable of burrowing through the ground"),
    swimming("Capable of swimming"),
    waterBreathing("Incapable of drowning"),
    hyperspaceCapable("You are capable of entering hyperspace"),
    extraPlanar("Capable of entering the Aether"),
    reach("Capable of attacking enemies in physical domains adjacent to your own."),
    masterworkWeapon(new DamageScalarContainer(0,0,0,0,.02),10,0,0,0,
            "Of great quality, granting +10 to hit, and +.02% to the damage scaling of the weapon's primary attribute."),
    masterworkArmor(new DamageScalarContainer(0,0,0,0,0),0,0,4,4,
            "Of great quality, granting +3 AC from the base value and +4 damage reduction"),
    testPools(new PoolValueContainer(1000,2000,3000,4000),"This is an OP test trait");

    private final StatContainer statModifiers; //TODO not implemented
    private final PoolValueContainer poolModifiers; //TODO not implemented
    private final DamageScalarContainer damageScalarContainer; //TODO not implemented
    private final Map<Skill, Integer> skillBonuses; //TODO not implemented
    private final int hitBonus;
    private final int damageBonus;
    private final int armorBonus;
    private final int damageReduction;

    private final String description;

    Trait(String description) {
        this(new StatContainer(), new PoolValueContainer(), new DamageScalarContainer(), Collections.emptyMap(), 0, 0, 0, 0, description);
    }

    Trait(StatContainer statModifiers,String description) {
        this(new StatContainer(), new PoolValueContainer(), new DamageScalarContainer(), Collections.emptyMap(), 0, 0, 0, 0, description);
    }

    Trait(PoolValueContainer poolModifiers, String description) {
        this(new StatContainer(), poolModifiers,new DamageScalarContainer(), Collections.emptyMap(), 0,0,0,0, description);
    }

    Trait(DamageScalarContainer damageScalarContainer, int hitBonus, int damageBonus, int armorBonus, int damageReduction, String description) {
        this(new StatContainer(), new PoolValueContainer(), damageScalarContainer, Collections.emptyMap(), hitBonus, damageBonus, armorBonus, damageReduction, description);
    }

    Trait(Map<Skill, Integer> skillBonuses, String description) {
        this(new StatContainer(), new PoolValueContainer(), new DamageScalarContainer(), skillBonuses, 0, 0, 0, 0, description);
    }

    Trait(int hitBonus, int damageBonus, int armorBonus, int damageReduction, String description) {
        this(new StatContainer(), new PoolValueContainer(), new DamageScalarContainer(), Collections.emptyMap(), hitBonus, damageBonus, armorBonus, damageReduction, description);
    }

    Trait(StatContainer statModifiers, PoolValueContainer poolModifiers, DamageScalarContainer damageScalarContainer, Map<Skill, Integer> skillBonuses, int hitBonus, int damageBonus, int armorBonus, int damageReduction, String description) {
        this.statModifiers = statModifiers;
        this.poolModifiers = poolModifiers;
        this.skillBonuses = skillBonuses;
        this.damageScalarContainer = damageScalarContainer;
        this.hitBonus = hitBonus;
        this.damageBonus = damageBonus;
        this.armorBonus = armorBonus;
        this.damageReduction = damageReduction;
        this.description = description;
    }

    public static String getSavableForm(Set<Trait> traits){
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for(Trait t:traits){
            if(first)
                first = false;
            else
                b.append(";");
            b.append(t);
        }
        return b.toString();
    }

    public String getDisplayableName(){
        return this.toString();
    }

    @Override
    public Attack modifyIncomingAttack(Attack in) {
        in.setBaseRoll(in.getBaseRoll() - armorBonus);
        in.setAttemptedDamage(in.getAttemptedDamage() - damageReduction);
        return in;
    }

    @Override
    public Attack modifyOutgoingAttack(Attack in) {
        in.setBaseRoll(in.getBaseRoll() + hitBonus);
        in.setAttemptedDamage(
                in.getAttemptedDamage() +
                damageBonus +
                damageScalarContainer.getDamageContribution(in.getAggressor().getStats())
        );
        return in;
    }

    /**
     * parses a trait from a string
     *
     * @param rawTrait the string value of the trait
     * @return either the Trait or null if not found
     */
    public static Trait getTraitFromSavable(String rawTrait) {
        try {
            return valueOf(rawTrait);
        } catch (EnumConstantNotPresentException e) {
            System.out.println("Failed to parse non-existent Trait enum: " + rawTrait);
        }
        return null;
    }

    /**
     * parses a bunch of ; separated trait strings into a Set of Traits.
     * @param rawEncodedTraits a ; separated list of trait strings
     * @return a Set of all Traits that could be parsed from the given string
     */
    public static Set<Trait> getTraitsFromSavable(String rawEncodedTraits) {
        String[] rawTraits = rawEncodedTraits.split(";");
        Set<Trait> traits = new HashSet<>();
        for (String rawTrait : rawTraits) {
            Trait t = getTraitFromSavable(rawTrait);
            if (t != null)
                traits.add(t);
        }
        return traits;
    }

    public StatContainer getStatModifiers() {
        return statModifiers;
    }

    public PoolValueContainer getPoolModifiers() {
        return poolModifiers;
    }

    public Map<Skill, Integer> getSkillBonuses() {
        return skillBonuses;
    }

    public int getHitBonus() {
        return hitBonus;
    }

    public int getDamageBonus() {
        return damageBonus;
    }

    public int getArmorBonus() {
        return armorBonus;
    }

    public int getDamageReduction() {
        return damageReduction;
    }

    public String getDescription() {
        return description;
    }
}
