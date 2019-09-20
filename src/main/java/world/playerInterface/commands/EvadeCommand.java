package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.client.ClientRegistry;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;
import main.java.world.entity.stance.EvasiveStance;
import main.java.world.notification.Notification;

import java.util.Locale;

import static main.java.world.playerInterface.ColorTheme.*;

public class EvadeCommand extends EntityCommand {
    private boolean complete = false;
    private String rawEvasion;

    public EvadeCommand(String rawEvasion, Client sourceClient, WorldModel model) {
        super(sourceClient, model);

        this.rawEvasion = rawEvasion;
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
    protected Skill getRequiredSkill() {
        return Skill.dodge1;
    }

    @Override
    protected void executeEntityCommand() {
        try {
            int evasion = Integer.parseInt(rawEvasion);

            if (evasion == 0) {
                //go to base stance
            } else if (evasion > 10) {
                //tell them to fuck off
            } else {
                getSourceEntity().setStance(new EvasiveStance(getSourceEntity(), evasion));
                //TODO notification
            }
        } catch (NumberFormatException e) {
            getSourceClient().sendMessage(getMessageInColor(rawEvasion + " is not an evasion level", FAILURE));
        }
        complete = true;
    }

    public class EvasionNotification extends Notification {
        private int evasion;
        private Entity sourceEntity;

        public EvasionNotification(Entity sourceEntity, int evasion, ClientRegistry registry) {
            super(registry);

            this.evasion = evasion;
            this.sourceEntity = sourceEntity;
        }

        @Override
        public String getAsMessage(Entity viewer) {
            String result;
            if (sourceEntity.equals(viewer)) {
                if (evasion == 0)
                    result = getMessageInColor("You relax out of your evasive stance", INFORMATIVE);
                else
                    result = getMessageInColor("You rise onto the balls of your feet", INFORMATIVE);
            } else {
                int hideRoll = sourceEntity.getSkills().performSkillCheck(
                        Skill.obscureIntent1, 0,
                        sourceEntity.getStats().getStat(Skill.obscureIntent1.getAssociatedStat()));
                int detectRoll = sourceEntity.getSkills().performSkillCheck(
                        Skill.perception1, 10,
                        sourceEntity.getStats().getStat(Skill.perception1.getAssociatedStat()));
                if (hideRoll >= detectRoll) {
                    result = "";
                } else if (evasion == 0) {
                    result = getMessageInColor(String.format(Locale.US, "You have passed a hidden (%s->%s) check(+%d)",
                            Skill.perception1.getDisplayName(), Skill.obscureIntent1.getDisplayName(), detectRoll - hideRoll), SUCCESS);
                    result += getMessageInColor("\n" + getEntityColored(sourceEntity,viewer,getWorldModel()) + " relaxes out of any stance",INFORMATIVE);
                } else if (detectRoll > hideRoll + 20) {
                    result = getMessageInColor(String.format(Locale.US, "You have passed a hidden (%s->%s) check(+%d)",
                            Skill.perception1.getDisplayName(), Skill.obscureIntent1.getDisplayName(), detectRoll - hideRoll), SUCCESS);

                    result += getMessageInColor(String.format(Locale.US, "\n%s seems %s(%d)",
                            getEntityColored(sourceEntity, viewer, getWorldModel()),
                            EvasiveStance.getEvasionDescriptor(evasion),
                            evasion), INFORMATIVE);
                } else {
                    result = getMessageInColor(String.format(Locale.US, "You have passed a hidden (%s->%s) check(+%d)",
                            Skill.perception1.getDisplayName(), Skill.obscureIntent1.getDisplayName(), detectRoll - hideRoll), SUCCESS);

                    result += getMessageInColor(String.format(Locale.US, "\n%s seems to want to evade",
                            getEntityColored(sourceEntity, viewer, getWorldModel())), INFORMATIVE);
                }
            }

            return result;
        }
    }
}
