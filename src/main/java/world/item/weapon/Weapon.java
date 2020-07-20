package main.java.world.item.weapon;

import main.java.world.entity.StatContainer;
import main.java.world.item.DamageType;
import main.java.world.item.Item;
import main.java.world.item.ItemFactory;
import main.java.world.item.ItemType;
import main.java.world.meta.World;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Random;

import static main.java.world.item.weapon.WeaponStatTable.*;

public class Weapon extends Item {
    private static final Random rnd = new Random();
    private DamageScalarContainer damageScalarContainer;

    public Weapon(ResultSet fromEntry, ItemFactory factory, String databaseName) throws Exception {
        super(fromEntry, factory, databaseName);
        initStats();
        damageScalarContainer = new DamageScalarContainer(this);
    }

    @Override
    public ItemType getItemType() {
        return ItemType.weapon;
    }

    @Override
    protected Map<String, String> getDerivedStats() {
        return WeaponStatTable.getStatsForWeapon(getItemName(), getDatabaseName());
    }

    @Override
    protected boolean compositeStatsExistInWorld(World targetWorld) {
        initStats();
        return WeaponStatTable.existsInWorld(getItemName(), targetWorld);
    }

    @Override
    protected boolean writeCompositeStatsToWorld(World targetWorld) {
        initStats();
        return WeaponStatTable.writeStatsToWorld(getDerivedStats(), targetWorld);
    }

    public double getStaminaUsage(int str, int dex) {
        double weight = super.getWeight();
        double balance = getBalance();
        double base = 5 + weight * 4;
        double strengthReduction = 0.05 * str;
        double balanceReduction = (50 - dex) / 100.0 * balance * weight;

        return (base - strengthReduction - balanceReduction) * 10;
    }

    public int rollDamage(StatContainer statContainer) {
        int dmgRange = getMaxBaseDamage() - getMinBaseDamage();

        int rawDmg = rnd.nextInt(dmgRange) + getMinBaseDamage();
        rawDmg += damageScalarContainer.getDamageContribution(statContainer);

        return rawDmg;
    }

    public int rollHit(StatContainer statContainer) {
        int rawRoll = rnd.nextInt(100);
        rawRoll = modWithPrimaryStat(rawRoll, statContainer);

        if (rawRoll < 0) {
            int simpleBonus = getSimpleBonus();
            if (rawRoll + simpleBonus > 0)
                rawRoll = 0;
            else
                rawRoll += simpleBonus;
        }

        rawRoll += getHitBonus();

        return rawRoll;
    }

    private int modWithPrimaryStat(int rawRoll, StatContainer statContainer) {
        int[] stats = {statContainer.getStrength(), statContainer.getDexterity(), statContainer.getIntelligence(), statContainer.getWisdom()};
        double[] scalars = {damageScalarContainer.getStrScalar(), damageScalarContainer.getDexScalar(), damageScalarContainer.getIntScalar(), damageScalarContainer.getWisScalar()};
        int bestIndex = 0;
        double bestScalar = -1.0f;
        for (int i = 0; i < scalars.length; i++) {
            if (scalars[i] > bestScalar || (scalars[i] == bestScalar && stats[i] > stats[bestIndex])) {
                bestIndex = i;
                bestScalar = scalars[i];
            }
        }
        return stats[bestIndex] - rawRoll;
    }

    public int getMinBaseDamage() {
        initStats();
        return getCastInt(MIN_BASE_DAMAGE);
    }

    public int getMaxBaseDamage() {
        initStats();
        return getCastInt(MAX_BASE_DAMAGE);
    }

    public float getAttackSpeed() {
        initStats();
        return getCastFloat(ATTACK_SPEED);
    }

    public float getBalance() {
        initStats();
        return getCastFloat(BALANCE);
    }

    public DamageScalarContainer getDamageScalars() {
        return damageScalarContainer;
    }

    public int getHitBonus() {
        initStats();
        return getCastInt(HIT_BONUS);
    }

    public int getSimpleBonus() {
        initStats();
        return getCastInt(SIMPLE_BONUS);
    }

    public DamageType getDamageType() {
        initStats();
        try {
            return DamageType.valueOf(getCastString(DAMAGE_TYPE));
        } catch (Exception ignored) {
        }
        return DamageType.slash;
    }

    private static ItemFactory.ItemParser parser = new ItemFactory.ItemParser() {
        @Override
        public ItemType getAssociatedType() {
            return ItemType.weapon;
        }

        @Override
        public Item parseFromResultSet(ResultSet fromEntry, ItemFactory sourceFactory, String databaseName) throws Exception {
            return new Weapon(fromEntry, sourceFactory, databaseName);
        }
    };

    public static class DamageScalarContainer {
        private final double strScalar;
        private final double dexScalar;
        private final double intScalar;
        private final double wisScalar;
        private final double primaryScalarBuff;

        public DamageScalarContainer() {
            this(0, 0, 0, 0, 0);
        }

        private DamageScalarContainer(Weapon readFrom) {
            this(readFrom.getCastDouble(STR_SCALAR),
                    readFrom.getCastDouble(DEX_SCALAR),
                    readFrom.getCastDouble(INT_SCALAR),
                    readFrom.getCastDouble(WIS_SCALAR),
                    0);
        }

        public DamageScalarContainer(double strScalar, double dexScalar, double intScalar, double wisScalar, double primaryScalarBuff) {
            this.strScalar = strScalar;
            this.dexScalar = dexScalar;
            this.intScalar = intScalar;
            this.wisScalar = wisScalar;
            this.primaryScalarBuff = primaryScalarBuff;
        }

        public DamageScalarContainer addValuesWith(DamageScalarContainer container) {
            return new DamageScalarContainer(
                    strScalar + container.strScalar,
                    dexScalar + container.dexScalar,
                    intScalar + container.intScalar,
                    wisScalar + container.wisScalar,
                    primaryScalarBuff + container.primaryScalarBuff
            );
        }

        public int getDamageContribution(StatContainer statContainer) {
            double strAddition = statContainer.getStrength() * getStrScalar();
            double dexAddition = statContainer.getDexterity() * getDexScalar();
            double intAddition = statContainer.getIntelligence() * getIntScalar();
            double wisAddition = statContainer.getWisdom() * getWisScalar();

            return (int) Math.floor(strAddition + dexAddition + intAddition + wisAddition);
        }

        public double getStrScalar() {
            if (strScalar >= dexScalar && strScalar >= intScalar && strScalar >= wisScalar)
                return strScalar + primaryScalarBuff;
            return strScalar;
        }

        public double getDexScalar() {
            if (dexScalar > strScalar && dexScalar > intScalar && dexScalar > wisScalar)
                return dexScalar + primaryScalarBuff;
            return dexScalar;
        }

        public double getIntScalar() {
            if (intScalar > strScalar && intScalar > dexScalar && intScalar > wisScalar)
                return intScalar + primaryScalarBuff;
            return intScalar;
        }

        public double getWisScalar() {
            if (wisScalar > strScalar && wisScalar > dexScalar && wisScalar > intScalar)
                return wisScalar + primaryScalarBuff;
            return wisScalar;
        }
    }

    public static ItemFactory.ItemParser factory() {
        return parser;
    }
}
