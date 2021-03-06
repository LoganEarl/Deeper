package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.client.ClientRegistry;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;
import main.java.world.entity.stance.BaseStance;
import main.java.world.entity.stance.EvasiveStance;
import main.java.world.entity.stance.Stance;
import main.java.world.notification.ConcreteNotification;
import main.java.world.notification.HiddenCheckNotification;

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
                notifyEntityRoom(new EvasionNotification(getSourceEntity(),evasion,getWorldModel().getRegistry()));
                getSourceEntity().setStance(new BaseStance());
            } else if (evasion > 10) {
                getSourceClient().sendMessage(getMessageInColor(rawEvasion + " is not an evasion level", FAILURE));
            } else {
                getSourceEntity().setStance(new EvasiveStance(getSourceEntity(), evasion));
                notifyEntityRoom(new EvasionNotification(getSourceEntity(),evasion,getWorldModel().getRegistry()));
            }
        } catch (NumberFormatException e) {
            getSourceClient().sendMessage(getMessageInColor(rawEvasion + " is not an evasion level", FAILURE));
        }
        complete = true;
    }

    public class EvasionNotification extends HiddenCheckNotification {
        private int evasion;
        private Entity sourceEntity;

        public EvasionNotification(Entity sourceEntity, int evasion, ClientRegistry registry) {
            super(10,Skill.perception1,0,Skill.obscureIntent1,sourceEntity,registry);

            this.evasion = evasion;
            this.sourceEntity = sourceEntity;
        }

        @Override
        public String getAsMessage(int relativeSuccess, Entity viewer) {
            String result;
            if (sourceEntity.equals(viewer)) {
                if (evasion == 0)
                    result = getMessageInColor("You relax out of your evasive stance", INFORMATIVE);
                else
                    result = getMessageInColor("You rise onto the balls of your feet", INFORMATIVE);
            } else {
                if (relativeSuccess < 0)
                    result = "";
                else if (evasion == 0)
                    result = getMessageInColor("\n" + getEntityColored(sourceEntity,viewer,getWorldModel()) + " relaxes out of any stance",INFORMATIVE);
                else if (relativeSuccess >= 20)
                    result = getMessageInColor(String.format(Locale.US, "%s seems %s(%d) evasive",
                            getEntityColored(sourceEntity, viewer, getWorldModel()),
                            Stance.getDegreeDescriptor(evasion),
                            evasion), INFORMATIVE);
                else
                    result = getMessageInColor(String.format(Locale.US, "\n%s seems to want to evade",
                            getEntityColored(sourceEntity, viewer, getWorldModel())), INFORMATIVE);
            }

            return result;
        }
    }
}
