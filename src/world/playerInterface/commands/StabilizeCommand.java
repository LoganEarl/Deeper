package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.WorldModel;
import world.entity.Entity;
import world.entity.skill.Skill;
import world.entity.stance.StablizedStance;
import world.notification.Notification;
import world.notification.NotificationSubscriber;
import world.room.RoomNotificationScope;

import java.awt.*;

import static world.playerInterface.ColorTheme.*;

public class StabilizeCommand extends EntityCommand {
    private String targetID;
    private boolean complete = false;
    private boolean usedStamina = false;

    private static final int STABILIZE_DIFFICULTY = 40;

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
        if (usedStamina) {
            int stat = getSourceEntity().getStats().getStat(getRequiredSkill().getAssociatedStat());
            int dex = getSourceEntity().getStats().getDexterity();
            //5-20 depending on primary stat and dex
            return (int) Math.ceil(20 - 7.5 * (stat / 100.0) - 7.5 * (dex / 100.0));
        }
        return 0;
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
        if (usedStamina) {
            int stat = getSourceEntity().getStats().getStat(getRequiredSkill().getAssociatedStat());
            //3000 to 6000 ms depending on dex
            long cooldown = (int) (stat / 100.0 * 3000 + 3000);
            getSourceEntity().setBalanceTime(cooldown, getSourceClient());
        }
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
            int difficulty = STABILIZE_DIFFICULTY;

            if (target.equals(getSourceEntity()))
                difficulty -= 20;

            int statLevel = getSourceEntity().getStats().getStat(getRequiredSkill().getAssociatedStat());

            int result = getSourceEntity().getSkills().performSkillCheck(getRequiredSkill(), difficulty, statLevel);

            if (result >= 0)
                getSourceEntity().setStance(new StablizedStance());
            notifyEntityRoom(new StabilizeNotification(getSourceEntity(), target, result, getWorldModel().getRegistry()));
            usedStamina = true;
        }

        complete = true;
    }

    public class StabilizeNotification extends Notification {
        private Entity stabilizer;
        private Entity stabilized;

        private int attemptScore;

        StabilizeNotification(Entity stabilizer, Entity stabilized, int attemptScore, ClientRegistry registry) {
            super(registry);

            this.stabilizer = stabilizer;
            this.stabilized = stabilized;
            this.attemptScore = attemptScore;
        }

        @Override
        public String getAsMessage(NotificationSubscriber viewer) {
            Entity viewerEnt = (Entity) viewer;

            String response;
            String stabilizerName = getEntityColored(stabilizer, viewerEnt, getWorldModel());
            String stabilizedName = getEntityColored(stabilized, viewerEnt, getWorldModel());
            String attempt = attemptScore >= 0 ? "succeed" : "fail";
            Color attemptColor = attemptScore >= 0 ? SUCCESS : FAILURE;

            if (viewerEnt.equals(stabilizer)) {
                if (stabilizer == stabilized)
                    response = getMessageInColor("You " + attempt + " in stabilizing yourself", attemptColor);
                else
                    response = getMessageInColor("You " + attempt + " in stabilizing " + stabilizedName, attemptColor);
            } else if (viewerEnt.equals(stabilized))
                response = getMessageInColor(stabilizerName + " " + attempt + "s in stabilizing you", INFORMATIVE);
            else {
                if (stabilizer == stabilized)
                    response = getMessageInColor(stabilizerName + " " + attempt + "s in stabilizing " + stabilizer.getReflexivePronoun(), INFORMATIVE);
                else
                    response = getMessageInColor(stabilizerName + " " + attempt + "s in stabilizing " + stabilizedName, INFORMATIVE);
            }

            return response;
        }
    }
}
