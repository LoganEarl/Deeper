package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.client.ClientRegistry;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;
import main.java.world.entity.skill.Skill;
import main.java.world.entity.stance.BaseStance;
import main.java.world.entity.stance.EvasiveStance;
import main.java.world.entity.stance.Stance;
import main.java.world.notification.HiddenCheckNotification;

import java.util.Locale;

import static main.java.world.playerInterface.ColorTheme.*;
import static main.java.world.playerInterface.ColorTheme.INFORMATIVE;

public class DeflectCommand extends EntityCommand {
    private boolean complete = false;
    private String rawDeflection;

    public DeflectCommand(String rawDeflection, Client sourceClient, WorldModel model) {
        super(sourceClient, model);

        this.rawDeflection = rawDeflection;
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
        return Skill.deflect1;
    }

    @Override
    protected void executeEntityCommand() {
        try {
            int evasion = Integer.parseInt(rawDeflection);

            if (evasion == 0) {
                notifyEntityRoom(new DeflectionNotification(getSourceEntity(),evasion,getWorldModel().getRegistry()));
                getSourceEntity().setStance(new BaseStance());
            } else if (evasion > 10) {
                getSourceClient().sendMessage(getMessageInColor(rawDeflection + " is not an deflection level", FAILURE));
            } else {
                getSourceEntity().setStance(new EvasiveStance(getSourceEntity(), evasion));
                notifyEntityRoom(new DeflectionNotification(getSourceEntity(),evasion,getWorldModel().getRegistry()));
            }
        } catch (NumberFormatException e) {
            getSourceClient().sendMessage(getMessageInColor(rawDeflection + " is not an deflection level", FAILURE));
        }
        complete = true;
    }

    public class DeflectionNotification extends HiddenCheckNotification {
        private int deflection;
        private Entity sourceEntity;

        public DeflectionNotification(Entity sourceEntity, int deflection, ClientRegistry registry) {
            super(10,Skill.perception1,0,Skill.obscureIntent1,sourceEntity,registry);

            this.deflection = deflection;
            this.sourceEntity = sourceEntity;
        }

        @Override
        public String getAsMessage(int relativeSuccess, Entity viewer) {
            String result;
            if (sourceEntity.equals(viewer)) {
                if (deflection == 0)
                    result = getMessageInColor("You relax out of your deflective stance", INFORMATIVE);
                else
                    result = getMessageInColor("You plant your feet and square your shoulders", INFORMATIVE);
            } else {
                if (relativeSuccess < 0)
                    result = "";
                else if (deflection == 0)
                    result = getMessageInColor("\n" + getEntityColored(sourceEntity,viewer,getWorldModel()) + " relaxes out of any stance",INFORMATIVE);
                else if (relativeSuccess >= 20)
                    result = getMessageInColor(String.format(Locale.US, "%s seems %s(%d) defencive",
                            getEntityColored(sourceEntity, viewer, getWorldModel()),
                            Stance.getDegreeDescriptor(deflection),
                            deflection), INFORMATIVE);
                else
                    result = getMessageInColor(String.format(Locale.US, "\n%s seems to want to defend",
                            getEntityColored(sourceEntity, viewer, getWorldModel())), INFORMATIVE);
            }

            return result;
        }
    }
}
