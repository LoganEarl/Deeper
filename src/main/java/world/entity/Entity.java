package main.java.world.entity;

import main.java.client.Client;
import main.java.database.DatabaseManager;
import main.java.world.WorldModel;
import main.java.world.entity.diplomacy.DiplomacyContainer;
import main.java.world.entity.equipment.EquipmentContainer;
import main.java.world.entity.pool.PoolContainer;
import main.java.world.entity.progression.ProgressionContainer;
import main.java.world.entity.race.Race;
import main.java.world.entity.skill.Skill;
import main.java.world.entity.skill.SkillContainer;
import main.java.world.entity.stance.BaseStance;
import main.java.world.entity.stance.Stance;
import main.java.world.item.weapon.Weapon;
import main.java.world.meta.World;
import main.java.world.notification.Notification;
import main.java.world.notification.NotificationSubscriber;
import main.java.world.room.Domain;
import main.java.world.room.Room;
import main.java.world.room.RoomConnection;
import main.java.world.trait.Trait;
import main.java.world.trait.Traited;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Stream;

import static main.java.world.entity.EntityTable.*;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Entity implements
        DatabaseManager.DatabaseEntry,
        NotificationSubscriber,
        Attack.Attackable, Attack.AttackOffenceModifier, Attack.AttackDefenceModifier,
        Traited {
    private static Map<String,Entity> entityCache = new HashMap<>();

    private String entityID;
    private String displayName;

    private LinkedHashMap<String, SqlExtender> extenders = new LinkedHashMap<>();

    private long balanceRecoveryTimestamp = 0;

    private String controllerType;
    private String roomName;
    private String raceID;

    private Stance currentStance;
    private Domain domain;

    private String databaseName;
    private WorldModel model;

    private static final String GET_SQL = String.format(Locale.US,"SELECT * FROM %s WHERE %s=?",TABLE_NAME,ENTITY_ID);
    private static final String DELETE_SQL = String.format(Locale.US,"DELETE FROM %s WHERE %s=?", TABLE_NAME,ENTITY_ID);
    private static final String GET_ROOM_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE (%s=?)",
            TABLE_NAME, ROOM_NAME);
    private static final String GET_NAME_ROOM_SQL = String.format(Locale.US, "SELECT * FROM %s WHERE (%s=? AND %s=?)", TABLE_NAME, DISPLAY_NAME, ROOM_NAME);

    /**Code returned when an entity has been successfully transferred to a new main.java.world by the transferToWorld() method*/
    public static final int CODE_TRANSFER_COMPLETE = 0;
    /**Code returned when an entity could not be transferred to the new main.java.world because there already exists an entity by that id in the target main.java.world*/
    public static final int CODE_ALREADY_EXISTS_AT_DESTINATION = -1;
    /**Code returned when an entity could not be transferred to the new main.java.world because of an unspecified database/file io error*/
    public static final int CODE_TRANSFER_FAILED = -2;

    private Entity() {
        //for use by the builder
    }

    private Entity(ResultSet readEntry, WorldModel model, String databaseName) throws Exception {
        this.databaseName = databaseName;

        extenders.put(PoolContainer.SIGNIFIER, new PoolContainer(readEntry));
        extenders.put(StatContainer.SIGNIFIER, new StatContainer(readEntry));
        extenders.put(EquipmentContainer.SIGNIFIER, new EquipmentContainer(readEntry, model.getItemCollection(), this));
        extenders.put(ProgressionContainer.SIGNIFIER, new ProgressionContainer(readEntry));
        extenders.put(DiplomacyContainer.SIGNIFIER, new DiplomacyContainer());

        entityID = readEntry.getString(ENTITY_ID);
        displayName = readEntry.getString(DISPLAY_NAME);

        raceID = readEntry.getString(RACE_ID);
        if(Race.getFromID(raceID, databaseName) == null)
            throw new IllegalArgumentException("Race of the entity is not recognized");

        controllerType = readEntry.getString(CONTROLLER_TYPE);
        String roomName = readEntry.getString(ROOM_NAME);
        setRoom(roomName);

        String rawDomain = readEntry.getString(DOMAIN);
        try{
            this.domain = Domain.valueOf(rawDomain);
        }catch (IllegalArgumentException e){
            domain = null;
            Room myRoom = Room.getByRoomName(roomName, databaseName);
            if(myRoom != null) domain = myRoom.getDefaultDomain();
            if(domain == null)
                throw new IllegalStateException("Entity does not have a domain");
        }

        getPools().calculatePoolMaxes(getStats());

        this.model = model;

        //this last to load the skills
        extenders.put(SkillContainer.SIGNIFIER, new SkillContainer(entityID, databaseName));

        setStance(new BaseStance());
    }

    /**
     * will transfer this entity to the given main.java.world, updating the meta file and everything.
     * If it fails, it will return the appropriate code and keep the entity in it's current main.java.world.
     * There cannot be more than one entity with the same Entity_ID in one main.java.world, attempting to move
     * an entity whose ID is also in the new main.java.world will fail the attempt
     * @param newWorld the new main.java.world this entity will exist in
     * @return ont of the CODE_* constants defined above.
     */
    public int transferToWorld(World newWorld){
        if(newWorld == null)
            throw new IllegalArgumentException("cannot transfer to a null world");

        if(existsInDatabase(databaseName) && !removeFromDatabase(databaseName)) {
            updateInDatabase(databaseName);
            return CODE_TRANSFER_FAILED;
        }

        if(existsInDatabase(newWorld.getDatabaseName()))
            return CODE_ALREADY_EXISTS_AT_DESTINATION;
        if(!saveToDatabase(newWorld.getDatabaseName()))
            return CODE_TRANSFER_FAILED;

        getEquipment().transferEquipmentToWorld(newWorld);

        if(!World.setWorldOfEntity(this,newWorld))
            return CODE_TRANSFER_FAILED;
        String tag = getEntityTag(entityID,databaseName);
        entityCache.remove(tag);
        this.databaseName = newWorld.getDatabaseName();

        Room newRoom = Room.getByRoomName(newWorld.getEntryRoomName(),newWorld.getDatabaseName());
        if(newRoom == null){
            System.out.println("Failed to transfer entity to nonexistent room [" + newWorld.getEntryRoomName() + "] in world " + newWorld.getWorldID());
            return CODE_TRANSFER_FAILED;
        }
        setRoom(newRoom);
        setDomain(newRoom.getDefaultDomain());

        tag = getEntityTag(entityID, databaseName);
        entityCache.put(tag,this);
        updateInDatabase(databaseName);
        return CODE_TRANSFER_COMPLETE;
    }

    @Override
    public Set<Trait> getInherentTraits() {
        HashSet<Trait> inherent = new HashSet<>();
        inherent.addAll(getSkills().getBestowedTraits());
        inherent.addAll(getRace().getBestowedTraits());

        return inherent;
    }

    @Override
    public Set<Trait> getTransitiveTraits() {
        Set<Trait> transitive = getInherentTraits();
        transitive.addAll(getEquipment().getBestowedTraits());

        return transitive;
    }

    public static Collection<Entity> getAllLoadedEntities(){
        return entityCache.values();
    }

    public static Entity getPlayableEntityByID(String entityID, WorldModel model){
        World w = World.getWorldOfEntityID(entityID);
        if(w != null) {
            String tag = getEntityTag(entityID, w.getDatabaseName());
            if(entityCache.containsKey(tag))
                return entityCache.get(tag);

            Entity entity = Entity.getEntityByEntityID(entityID, model, w.getDatabaseName());
            if(entity != null)
                entityCache.put(tag, entity);
            return entity;
        }
        return null;
    }

    public static Entity getEntityByDisplayName(String displayName, String roomName, WorldModel model, String databaseName){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Entity toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_NAME_ROOM_SQL);
                getSQL.setString(1,displayName);
                getSQL.setString(2,roomName);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new Entity(accountSet, model, databaseName);
                else
                    toReturn = null;
                getSQL.close();
                //c.close();
            }catch (Exception e){
                toReturn = null;
            }
        }

        if(toReturn != null){
            String tag = getEntityTag(toReturn.getID(),toReturn.getDatabaseName());
            if(entityCache.containsKey(tag))
                return entityCache.get(tag);
            else
                entityCache.put(tag,toReturn);
        }

        return toReturn;
    }

    public static Entity getEntityByEntityID(String entityID, WorldModel model, String databaseName){
        String tag = getEntityTag(entityID,databaseName);
        if(entityCache.containsKey(tag))
            return entityCache.get(tag);

        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        Entity toReturn;
        if(c == null)
            return null;
        else{
            try {
                getSQL = c.prepareStatement(GET_SQL);
                getSQL.setString(1,entityID);
                ResultSet accountSet = getSQL.executeQuery();
                if(accountSet.next())
                    toReturn = new Entity(accountSet, model, databaseName);
                else
                    toReturn = null;
                getSQL.close();
                //c.close();
            }catch (Exception e){
                toReturn = null;
            }
        }

        if(toReturn != null){
            entityCache.put(tag,toReturn);
        }

        return toReturn;
    }

    /**
     * gets all the entities in the given room
     * @param roomName the room name to check for items
     * @param databaseName the database containing the items
     * @return a list of all items in the room
     */
    public static List<Entity> getEntitiesInRoom(String roomName, String databaseName, WorldModel model, String... excludedEntityIDs){
        Connection c = DatabaseManager.getDatabaseConnection(databaseName);
        PreparedStatement getSQL;
        List<Entity> foundItems = new LinkedList<>();
        if(c == null)
            return Collections.emptyList();
        else{
            try {
                List<String> excluded;
                if(excludedEntityIDs == null || excludedEntityIDs.length == 0)
                    excluded = Collections.emptyList();
                else
                    excluded = Arrays.asList(excludedEntityIDs);

                getSQL = c.prepareStatement(GET_ROOM_SQL);
                getSQL.setString(1,roomName);
                ResultSet accountSet = getSQL.executeQuery();
                while(accountSet.next()) {
                    Entity e = new Entity(accountSet, model, databaseName);
                    if(!excluded.contains(e.getID()))
                        foundItems.add(e);
                }
                getSQL.close();
                //c.close();
            }catch (Exception e){
                foundItems = Collections.emptyList();
            }
        }

        for(int i = 0; i <foundItems.size(); i++){
            String tag = getEntityTag(foundItems.get(i).getID(), foundItems.get(i).getDatabaseName());
            if(entityCache.containsKey(tag))
                foundItems.set(i,entityCache.get(tag));
            else
                entityCache.put(tag,foundItems.get(i));
        }

        return foundItems;
    }

    @Override
    public boolean saveToDatabase(String databaseName) {
        Entity entity = getEntityByEntityID(entityID, model, databaseName);
        if (entity == null) {
            boolean success = DatabaseManager.executeStatement(makeInsertSQL(extenders,
                    ENTITY_ID, DISPLAY_NAME, CONTROLLER_TYPE, ROOM_NAME, RACE_ID), databaseName, appendData(extenders,
                    entityID, displayName, controllerType, roomName, raceID)) > 0;
            if (success)
                entityCache.put(getEntityTag(entityID, databaseName), this);
            return success;
        } else {
            return updateInDatabase(databaseName);
        }
    }

    @Override
    public boolean removeFromDatabase(String databaseName) {
        boolean success = DatabaseManager.executeStatement(DELETE_SQL,databaseName, entityID) > 0;
        if(success)
            entityCache.remove(getEntityTag(entityID,databaseName));
        return success;
    }

    @Override
    public boolean updateInDatabase(String databaseName) {
        boolean success = DatabaseManager.executeStatement(makeInsertSQL(extenders,
                ENTITY_ID,DISPLAY_NAME,CONTROLLER_TYPE,ROOM_NAME,RACE_ID, DOMAIN), databaseName, appendData(extenders,
                entityID,displayName,controllerType,roomName,raceID, domain.name())) > 0;
        if(success)
            entityCache.put(getEntityTag(entityID,databaseName),this);
        return success;
    }

    @Override
    public boolean existsInDatabase(String databaseName) {
        return getEntityByEntityID(entityID,model,databaseName) != null;
    }

    private static String makeInsertSQL(LinkedHashMap<String, SqlExtender> extenders, String... baseColumns){
        StringBuilder statement = new StringBuilder("REPLACE INTO ").append(EntityTable.TABLE_NAME).append("(");
        StringBuilder questionMarks = new StringBuilder("(");
        boolean first = true;
        for(String column:baseColumns){
            if(first)
                first = false;
            else {
                statement.append(",");
                questionMarks.append(",");
            }
            statement.append(column);
            questionMarks.append("?");
        }
        for(SqlExtender extender: extenders.values()){
            for(String column: extender.getSqlColumnHeaders()){
                statement.append(",").append(column);
                questionMarks.append(",").append("?");
            }
        }
        return statement.append(") VALUES ").append(questionMarks).append(")").toString();
    }

    private static Object[] appendData(LinkedHashMap<String,SqlExtender> extenders, Object... additionalParams){
        List<Object[]> arrays = new ArrayList<>();
        for(SqlExtender extender:extenders.values())
            arrays.add(extender.getInsertSqlValues());
        for(Object[] toAdd: arrays)
            additionalParams = Stream.of(additionalParams,toAdd).flatMap(Stream::of).toArray();
        return additionalParams;
    }

    public String getID(){
        return entityID;
    }

    /**
     * get a displayable name for this entity.
     * @return The display name if there is one. Otherwise will return the race + : + entity ID (if the race is illegal as well it will just return the entityID)
     */
    public String getDisplayName() {
        if(displayName != null && !displayName.isEmpty()) {
            return displayName;
        }else{
            Race myRace = Race.getFromID(this.raceID, databaseName);
            if(myRace == null)
                return this.entityID;
            else
                return myRace.getDisplayName() + ":" + this.entityID;
        }
    }

    @Override
    public Attack receiveAttack(Attack attack) {
        attack.setDidLand(attack.getBaseRoll() >= 0);
        if(attack.getDidLand()) {
            boolean isDying = getPools().isDying();
            boolean isDead = getPools().isDead();

            getPools().damage(attack.getDamageDealt());
            attack.setDamageDealt(attack.getDamageDealt());

            if(getPools().isDying() != isDying)
                attack.setDidEnterDyingState(getPools().isDying());
            if(getPools().isDead() != isDead)
                attack.setDidDie(getPools().isDead());

            attack.complete();
        }
        saveToDatabase(databaseName);

        return attack;
    }

    @Override
    public Attack produceAttackWithWeapon(Weapon weapon, int situationalHitBonus) {
        StatContainer myStats = getStats();

        int roll = weapon.rollHit(getStats()) + situationalHitBonus;
        int damage = weapon.rollDamage(getStats());

        return new Attack()
                .setBaseRoll(roll)
                .setAttemptedDamage(damage);
    }

    @Override
    public Attack modifyIncomingAttack(Attack in) {
        in = getEquipment().modifyIncomingAttack(in);
        in = currentStance.modifyIncomingAttack(in);

        return in;
    }

    @Override
    public Attack modifyOutgoingAttack(Attack in) {
        in = currentStance.modifyOutgoingAttack(in);

        return in;
    }

    public void setStance(Stance stance){
        Skill requiredSkill = stance.getRequiredSkill();
        if(requiredSkill == null || getSkills().getLearnLevel(requiredSkill) != SkillContainer.UNLEARNED) {
            if (!stance.equals(currentStance)) {
                this.currentStance = stance;
            }
        }
    }

    public Stance getStance() {
        return currentStance;
    }

    public String getControllerType() {
        return controllerType;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoom(Room newRoom){
        if(newRoom != null) {
            this.roomName = newRoom.getRoomName();
            newRoom.detectTriviallyVisibleConnections(this);
        }
    }

    public void setRoom(String roomName){
        setRoom(Room.getByRoomName(roomName, databaseName));
    }

    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Shortcut to Race.getFromID(myEntity.getRaceID())
     * @return the instantiated form of this entity's race. Can be null
     */
    public Race getRace(){
        return Race.getFromID(this.raceID, databaseName);
    }

    public String getRaceID(){
        return this.raceID;
    }

    public EquipmentContainer getEquipment(){
        return (EquipmentContainer)extenders.get(EquipmentContainer.SIGNIFIER);
    }

    public PoolContainer getPools(){
        return (PoolContainer) extenders.get(PoolContainer.SIGNIFIER);
    }

    public StatContainer getStats(){
        return (StatContainer) extenders.get(StatContainer.SIGNIFIER);
    }

    public SkillContainer getSkills() {
        return (SkillContainer) extenders.get(SkillContainer.SIGNIFIER);
    }

    public DiplomacyContainer getDiplomacy() {
        return (DiplomacyContainer) extenders.get(DiplomacyContainer.SIGNIFIER);
    }

    public ProgressionContainer getProgression(){
        return (ProgressionContainer) extenders.get(ProgressionContainer.SIGNIFIER);
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    /**
     * gets if the entity is off cool-down and ready to act again
     * @return true if ready to act
     */
    public boolean isBalanced(){
        return System.currentTimeMillis() >= balanceRecoveryTimestamp;
    }
    /**
     * puts the entity on cool-down, Allowing them to act once they have recovered their balance from an action
     * @param waitMs how long they should remain imbalanced before recovering
     */
    public void setBalanceTime(long waitMs){
        setBalanceTime(waitMs,null);
    }

    /**
     * puts the entity on cool-down, Allowing them to act once they have recovered their balance from an action
     * @param waitMs how long they should remain imbalanced before recovering
     * @param attachedClient an optional client to notify once balance is restored
     */
    public void setBalanceTime(long waitMs, Client attachedClient){
        if(waitMs > 0) {
            balanceRecoveryTimestamp = System.currentTimeMillis() + waitMs;
            if (attachedClient != null) {
                attachedClient.sendMessage("You recover your balance", balanceRecoveryTimestamp);
            }
        }
    }

    private static String getEntityTag(String entityID, String databaseName){
        return "<!ID!>"+entityID+"<!LOCATION!>" + databaseName;
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class EntityBuilder{
        private String entityID = "";
        private String displayName = "";
        private int hp = 10;
        private int maxHP = 10;
        private int mp = 10;
        private int maxMP = 10;
        private int stamina = 10;
        private int maxStamina = 10;
        private int burnout = 10;
        private int maxBurnout = 10;

        private int strength = 10;
        private int dexterity = 10;
        private int intelligence = 10;
        private int wisdom = 10;
        private int toughness = 10;
        private int fitness = 10;

        private String controllerType = CONTROLLER_TYPE_STATIC;
        private String roomName = "";
        private String raceID = Race.HUMAN.getRaceID();

        private String databaseName = "";
        private WorldModel model;

        public EntityBuilder(WorldModel model){
            this.model = model;
        }

        public Entity build(){
            Entity e = new Entity();
            LinkedHashMap<String,SqlExtender> extenders = new LinkedHashMap<>();
            extenders.put(PoolContainer.SIGNIFIER, new PoolContainer(hp,maxHP,mp,maxMP,stamina,maxStamina,burnout,maxBurnout));
            extenders.put(StatContainer.SIGNIFIER,new StatContainer(strength,dexterity,intelligence,wisdom, toughness, fitness));
            extenders.put(EquipmentContainer.SIGNIFIER, new EquipmentContainer(model.getItemCollection(), e));
            extenders.put(ProgressionContainer.SIGNIFIER, new ProgressionContainer(e));
            extenders.put(DiplomacyContainer.SIGNIFIER, new DiplomacyContainer());
            extenders.put(SkillContainer.SIGNIFIER, new SkillContainer(entityID, databaseName));

            e.extenders = extenders;
            e.entityID = entityID;
            e.displayName = displayName;
            e.controllerType = controllerType;
            e.setRoom(roomName);
            e.raceID = raceID;
            e.databaseName = databaseName;

            e.getPools().calculatePoolMaxes(e.getStats());
            e.getPools().fill();
            //TODO remove this
            e.getProgression().earnIP(100000);

            e.setStance(new BaseStance());

            return e;
        }

        public EntityBuilder setHPVals(int hp, int maxHP){
            this.hp = hp;
            this.maxHP = maxHP;
            return this;
        }

        public EntityBuilder setMPVals(int mp, int maxMP){
            this.mp = mp;
            this.maxMP = maxMP;
            return this;
        }

        public EntityBuilder setStaminaVals(int stamina, int maxStamina){
            this.stamina = stamina;
            this.maxStamina = maxStamina;
            return this;
        }

        public EntityBuilder setStrength(int strength){
            this.strength = strength;
            return this;
        }

        public EntityBuilder setDexterity(int dexterity){
            this.dexterity = dexterity;
            return this;
        }

        public EntityBuilder setIntelligence(int intelligence){
            this.intelligence = intelligence;
            return this;
        }

        public EntityBuilder setWisdom(int wisdom){
            this.wisdom = wisdom;
            return this;
        }

        public void setToughness(int toughness) {
            this.toughness = toughness;
        }

        public void setFitness(int fitness) {
            this.fitness = fitness;
        }

        public EntityBuilder setID(String id){
            this.entityID = id;
            return this;
        }

        public EntityBuilder setDisplayName(String displayName){
            this.displayName = displayName;
            return this;
        }

        public EntityBuilder setDatabaseName(String databaseName){
            this.databaseName = databaseName;
            return this;
        }

        public EntityBuilder setRoomName(String roomName){
            this.roomName = roomName;
            return this;
        }

        public EntityBuilder setRace(Race r){
            this.raceID = r.getRaceID();
            return this;
        }

        public void setBurnout(int burnout) {
            this.burnout = burnout;
        }

        public void setMaxBurnout(int maxBurnout) {
            this.maxBurnout = maxBurnout;
        }

        /**
         * sets the race of the building entity and also sets it's stats to the defaults specified in the race
         * @param r the race to pull data from
         * @return this builder
         */
        public EntityBuilder setStatsToRaceDefaults(Race r){
            this.raceID = r.getRaceID();
            this.strength =r.getBaseStr();
            this.dexterity = r.getBaseDex();
            this.intelligence = r.getBaseInt();
            this.wisdom = r.getBaseWis();
            this.fitness = r.getBaseFit();
            this.toughness = r.getBaseTough();
            return this;
        }

        /**
         * sets the controller
         * @param controllerType one of the EntityTable.CONTROLLER_TYPE_* constants
         * @throws IllegalArgumentException if passed in controller type is not one of the EntityTable.CONTROLLER_TYPE_* constants
         * @return this builder
         */
        public EntityBuilder setControllerType(String controllerType){
            if(controllerType != null && (controllerType.equals(CONTROLLER_TYPE_PLAYER) || controllerType.equals(CONTROLLER_TYPE_STATIC)))
                this.controllerType = controllerType;
            else
                throw new IllegalArgumentException("Cannot assign a controller type that is not one of the EntityTable.CONTROLLER_TYPE_* constants");
            return this;
        }
    }

    public String getPronoun(){
        return "it";
    }

    public String getPossessivePronoun(){
        return "its";
    }

    public String getReflexivePronoun(){
        return "itself";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;
        if(obj instanceof Entity){
            Entity that = (Entity) obj;
            return that.getID().equals(this.getID()) && that.getDatabaseName().equals(this.getDatabaseName());
        }
        return false;
    }

    @Override
    public void notify(Notification notification) {
        if(EntityTable.CONTROLLER_TYPE_PLAYER.equals(getControllerType())) {
            Client toNotify = notification.getClientRegistry().getClient(entityID);
            String message = notification.getAsMessage(this);
            if(message != null && !message.isEmpty())
                toNotify.sendMessage(message);
        }
    }

    public interface SqlExtender {
        String getSignifier();
        Object[] getInsertSqlValues();
        String[] getSqlColumnHeaders();
    }
}
