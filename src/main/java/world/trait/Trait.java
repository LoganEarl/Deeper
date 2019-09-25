package main.java.world.trait;

import main.java.world.entity.Attack;

public enum Trait implements Attack.AttackDefenceModifier, Attack.AttackOffenceModifier {
    //Traversal
    flight,
    burrowing,
    swimming,
    waterBreathing,
    hyperspaceCapable,
    extraPlanar;

    //Information
    public static Trait getTraitFromSavable(String rawTrait){
        try {
            return valueOf(rawTrait);
        }catch (Exception ignored){}
        return null;
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
