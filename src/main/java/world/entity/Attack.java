package main.java.world.entity;

import main.java.world.item.DamageType;
import main.java.world.item.weapon.Weapon;

public class Attack {
    public interface AttackDefenceModifier {
        Attack modifyIncomingAttack(Attack in);
    }

    public interface AttackOffenceModifier {
        Attack modifyOutgoingAttack(Attack in);
    }

    public interface Attackable {
        /**The starting point for the attack chain*/
        Attack produceAttackWithWeapon(Weapon weapon, int situationalHitBonus);
        Attack receiveAttack(Attack in);
    }

    private int attemptedDamage = 0;
    private int damageDealt = 0;
    private DamageType damageType = DamageType.crush;
    private Weapon attackWeapon = null;
    private int baseRoll = 0;
    private Entity aggressor;
    private Entity defender;
    private boolean didDeflect = false;
    private boolean didDodge = false;
    private boolean didLand = false;
    private boolean didEnterDyingState = false;
    private boolean didDie = false;

    private boolean isLocked = false;

    public Attack complete() {
        isLocked = true;
        return this;
    }

    public int getAttemptedDamage() {
        return attemptedDamage;
    }

    public Attack setAttemptedDamage(int attemptedDamage) {
        if (!isLocked)
            this.attemptedDamage = attemptedDamage;
        return this;
    }

    public Weapon getAttackWeapon() {
        return attackWeapon;
    }

    public Attack setAttackWeapon(Weapon attackWeapon) {
        if (!isLocked)
            this.attackWeapon = attackWeapon;
        return this;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public boolean getDidDeflect() {
        return didDeflect;
    }

    public boolean getDidDodge() {
        return didDodge;
    }

    public boolean getDidEnterDyingState() {
        return didEnterDyingState;
    }

    public boolean getDidDie() {
        return didDie;
    }

    public int getBaseRoll() {
        return baseRoll;
    }

    public Entity getAggressor() {
        return aggressor;
    }

    public Entity getDefender() {
        return defender;
    }

    public DamageType getDamageType(){
        return damageType;
    }

    public boolean getDidLand(){
        return didLand;
    }

    public Attack setDidLand(boolean didLand){
        if(!isLocked)
            this.didLand = didLand;
        return this;
    }

    public Attack setDamageType(DamageType type){
        if(!isLocked)
            this.damageType = type;
        return this;
    }

    public Attack setDamageDealt(int damageDealt) {
        if (!isLocked)
            this.damageDealt = damageDealt;
        return this;
    }

    public Attack setBaseRoll(int baseRoll) {
        if (!isLocked)
            this.baseRoll = baseRoll;
        return this;
    }

    public Attack setAggressor(Entity aggressor) {
        if (!isLocked)
            this.aggressor = aggressor;
        return this;
    }

    public Attack setDefender(Entity defender) {
        if (!isLocked)
            this.defender = defender;
        return this;
    }

    public Attack setDidDeflect(boolean didDeflect) {
        if (!isLocked)
            this.didDeflect = didDeflect;
        return this;
    }

    public Attack setDidDodge(boolean didDodge) {
        if (!isLocked)
            this.didDodge = didDodge;
        return this;
    }

    public Attack setDidEnterDyingState(boolean didEnterDyingState) {
        if (!isLocked)
            this.didEnterDyingState = didEnterDyingState;
        return this;
    }

    public Attack setDidDie(boolean didDie) {
        if (!isLocked)
            this.didDie = didDie;
        return this;
    }
}
