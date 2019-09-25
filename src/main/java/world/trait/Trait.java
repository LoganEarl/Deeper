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

    @Override
    public Attack modifyIncomingAttack(Attack in) {
        return in;
    }

    @Override
    public Attack modifyOutgoingAttack(Attack in) {
        return in;
    }
}
