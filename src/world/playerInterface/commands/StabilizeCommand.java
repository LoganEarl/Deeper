package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.WorldModel;
import world.entity.Entity;
import world.entity.skill.Skill;
import world.entity.skill.SkillTable;
import world.notification.Notification;
import world.notification.NotificationSubscriber;

import java.awt.*;

import static world.playerInterface.ColorTheme.*;

public class StabilizeCommand extends EntityCommand {
    private String targetID;
    private boolean complete = false;

    /**
     * sole constructor
     *
     * @param sourceClient the client attempting the command
     * @param targetID     the target to stabilize. If empty or null, the client is assumed to be stabilizing themself.
     * @param model        the world model
     */
    public StabilizeCommand(Client sourceClient, String targetID, WorldModel model) {
        super(sourceClient, model);

        if (targetID == null)
            targetID = "";
        this.targetID = targetID;
    }

    @Override
    protected Skill getRequiredSkill() {
        return Skill.stabilize1;
    }

    @Override
    protected int getRequiredStamina() {
        return super.getRequiredStamina();
    }

    @Override
    protected int getStaminaUsed() {
        return super.getStaminaUsed();
    }

    @Override
    protected boolean requiresBalance() {
        return true;
    }

    @Override
    protected boolean entityCommandIsComplete() {
        return complete;
    }

    @Override
    protected void setBalance() {
        super.setBalance();
    }

    @Override
    protected void executeEntityCommand() {
        Entity target;

        if (targetID.isEmpty())
            target = getSourceEntity();
        else
            target = getWorldModel().getEntityCollection().getEntityByDisplayName(targetID, getSourceEntity().getRoomName(), getSourceEntity().getDatabaseName());

        if (target == null)
            target = getWorldModel().getEntityCollection().getEntityByEntityID(targetID, getSourceEntity().getDatabaseName());
        if (target == null)
            getSourceClient().sendMessage("There is " + getMessageInColor("nothing named " + targetID + " nearby", FAILURE));
        else {
            int difficulty = (int)Skill.recover1.getBonusAmount();

            if(SkillTable.entityHasSkill(getSourceEntity(),Skill.recover4))
                difficulty = (int)Skill.recover4.getBonusAmount();
            else if(SkillTable.entityHasSkill(getSourceEntity(),Skill.recover3))
                difficulty = (int)Skill.recover3.getBonusAmount();
            else if(SkillTable.entityHasSkill(getSourceEntity(),Skill.recover2))
                difficulty = (int)Skill.recover2.getBonusAmount();

            if(target.equals(getSourceEntity()))
                difficulty -= 20;

            int result = getSourceEntity().getPools()
        }
    }

    public class StabilizeNotification extends Notification{
        private Entity stabilizer;
        private Entity stabilized;

        private int attemptScore;

        public StabilizeNotification(Entity stabilizer, Entity stabilized, int attemptScore, ClientRegistry registry) {
            super(registry);

            this.stabilizer = stabilizer;
            this.stabilized = stabilized;
            this.attemptScore = attemptScore;
        }

        @Override
        public String getAsMessage(NotificationSubscriber viewer) {
            Entity viewerEnt = (Entity)viewer;

            String response;
            String stabilizerName = getEntityColored(stabilizer,viewerEnt,getWorldModel());
            String stabilizedName = getEntityColored(stabilized,viewerEnt,getWorldModel());
            String attempt = attemptScore >=0? "succeed":"fail";
            Color attemptColor = attemptScore >=0? SUCCESS:FAILURE;

            if(viewerEnt.equals(stabilizer)){
                if(stabilizer == stabilized)
                    response = getMessageInColor("You " + attempt + " in stabilizing yourself",attemptColor);
                else
                    response = getMessageInColor("You " + attempt + " in stabilizing " + stabilizedName,attemptColor);
            }
            else if(viewerEnt.equals(stabilized))
                response = getMessageInColor(stabilizerName + " " + attempt + "s in stabilizing you",INFORMATIVE);
            else{
                if(stabilizer == stabilized)
                    response = getMessageInColor(stabilizerName + " " + attempt + "s in stabilizing " + stabilizer.getReflexivePronoun(),INFORMATIVE);
                else
                    response = getMessageInColor(stabilizerName + " " + attempt + "s in stabilizing " + stabilizedName,INFORMATIVE);
            }

            return response;
        }
    }
}
