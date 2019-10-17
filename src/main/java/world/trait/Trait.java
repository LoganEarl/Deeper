package main.java.world.trait;
import main.java.world.entity.Attack;

import java.util.HashSet;
import java.util.Set;

public enum Trait implements Attack.AttackDefenceModifier, Attack.AttackOffenceModifier {
    //Traversal
    flight,
    burrowing,
    swimming,
    waterBreathing,
    hyperspaceCapable,
    extraPlanar,

    reach,
    masterwork{
        @Override
        public Attack modifyOutgoingAttack(Attack in) {
            in.setBaseRoll(in.getBaseRoll() + 5);
            in.setAttemptedDamage((int)(in.getAttemptedDamage() * 1.1));
            return in;
        }
    }





    ;

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
        return in;
    }

    @Override
    public Attack modifyOutgoingAttack(Attack in) {
        return in;
    }
}
