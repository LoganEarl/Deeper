package main.java.world.entity.pool;

public class PoolValueContainer {
    private int hp;
    private int mp;
    private int stamina;
    private int burnout;

    public PoolValueContainer() {
        this(0,0,0,0);
    }

    public PoolValueContainer(PoolValueContainer copyFrom){
        this(copyFrom.hp, copyFrom.mp, copyFrom.stamina, copyFrom.burnout);
    }

    public PoolValueContainer(int hp, int mp, int stamina, int burnout) {
        this.hp = hp;
        this.mp = mp;
        this.stamina = stamina;
        this.burnout = burnout;
    }

    public void clamp(PoolValueContainer maxValues){
        if (hp > maxValues.hp) hp = maxValues.hp;
        if (mp > maxValues.mp) mp = maxValues.mp;
        if (stamina > maxValues.stamina) stamina = maxValues.stamina;
        if (burnout > maxValues.burnout) burnout = maxValues.burnout;
    }

    public int getHp() {
        return hp;
    }

    public int getMp() {
        return mp;
    }

    public int getStamina() {
        return stamina;
    }

    public int getBurnout() {
        return burnout;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public void setBurnout(int burnout) {
        this.burnout = burnout;
    }

    public void addHp(int hp){
        this.hp += hp;
    }

    public void addMp(int mp){
        this.mp += mp;
    }

    public void addStamina(int stamina){
        this.stamina += stamina;
    }

    public void addBurn(int burn){
        this.burnout += burn;
    }
}
