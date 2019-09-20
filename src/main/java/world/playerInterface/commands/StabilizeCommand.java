package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.client.ClientRegistry;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;
import main.java.world.entity.stance.StabilizedStance;
import main.java.world.notification.Notification;

import java.awt.*;

import static main.java.world.playerInterface.ColorTheme.*;

public class StabilizeCommand extends EntityCommand {
    private String targetID;
    private boolean complete = false;
    private boolean usedStamina = false;

    private static final int STABILIZE_DIFFICULTY = 40;

    /**
     * sole constructor
     *
     * @param sourceClient the main.java.client attempting the command
     * @param targetID     the target to stabilize. If empty or null, the main.java.client is assumed to be stabilizing themself.
     * @param model        the main.java.world model
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
            //50-200 depending on primary stat and dex
            return 10 * (int) Math.ceil(20 - 7.5 * (stat / 100.0) - 7.5 * (dex / 100.0));
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
                getSourceEntity().setStance(new StabilizedStance());
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
        public String getAsMessage(Entity viewer) {
            String response;
            String stabilizerName = getEntityColored(stabilizer, viewer, getWorldModel());
            String stabilizedName = getEntityColored(stabilized, viewer, getWorldModel());
            String attempt = attemptScore >= 0 ? "succeed" : "fail";
            Color attemptColor = attemptScore >= 0 ? SUCCESS : FAILURE;

            if (viewer.equals(stabilizer)) {
                if (stabilizer == stabilized)
                    response = getMessageInColor("You " + attempt + " in stabilizing yourself", attemptColor);
                else
                    response = getMessageInColor("You " + attempt + " in stabilizing " + stabilizedName, attemptColor);
            } else if (viewer.equals(stabilized))
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
