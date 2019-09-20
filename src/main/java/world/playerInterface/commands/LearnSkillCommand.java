package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.client.ClientRegistry;
import main.java.network.CommandExecutor;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;
import main.java.world.entity.progression.ProgressionContainer;
import main.java.world.entity.skill.Skill;
import main.java.world.notification.Notification;

import static main.java.world.playerInterface.ColorTheme.*;

public class LearnSkillCommand extends EntityCommand {
    private boolean complete = false;
    private String skillName;
    private MessagePipeline pipeline;

    public LearnSkillCommand(String skillName, Client sourceClient, MessagePipeline messagePipeline, WorldModel model) {
        super(sourceClient, model);

        this.pipeline = messagePipeline;

        this.skillName = skillName;
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
        //do not take balance
    }

    @Override
    protected void executeEntityCommand() {
        Skill baseSkill = Skill.getSkillOfDisplayName(skillName, 0);
        if (baseSkill == null || !baseSkill.isVisibleToEntity(getSourceEntity())) {
            getSourceClient().sendMessage(getMessageInColor("You know of no skill called " + skillName,FAILURE));
        } else {
            int existingKnowledgeLevel = getSourceEntity().getSkills().getLearnLevel(baseSkill);
            int targetKnowledgeLevel = existingKnowledgeLevel + 1;
            Skill toLearn = Skill.getSkillOfDisplayName(skillName, targetKnowledgeLevel);
            if (toLearn == null || !toLearn.isVisibleToEntity(getSourceEntity()))
                getSourceClient().sendMessage(getMessageInColor("You are not aware of a way to improve your current technique",FAILURE));
            else if (!toLearn.isLearnableByEntity(getSourceEntity()))
                getSourceClient().sendMessage(getMessageInColor("You are unable to learn " + toLearn.getDisplayName(),FAILURE));
            else if (getSourceEntity().getProgression().getIP() < toLearn.getIPCost())
                getSourceClient().sendMessage(getMessageInColor("You lack the information potential to learn " + toLearn.getDisplayName(),FAILURE));
            else {
                LearningController controller = new LearningController(getSourceEntity(), toLearn, getWorldModel().getRegistry());
                getWorldModel().getExecutor().scheduleCommand(controller);
                pipeline.addMessageContext(getSourceClient(), controller);

            }
        }
        complete = true;
    }

    public class LearningController implements MessagePipeline.MessageContext, CommandExecutor.Command {
        private Entity sourceEntity;
        private Skill toLearn;
        private ClientRegistry registry;

        private long completionTimestamp;
        private long startTimestamp;
        private double lastPercentage;

        private boolean canceled = false;
        private boolean controllerComplete = false;

        private long lastUpdateTime = 0;

        LearningController(Entity sourceEntity, Skill toLearn, ClientRegistry registry) {
            this.sourceEntity = sourceEntity;
            this.toLearn = toLearn;
            this.registry = registry;

            startTimestamp = System.currentTimeMillis();
            completionTimestamp = System.currentTimeMillis() + (toLearn.getIPCost() / 200 * 1000);

            notifyEntityRoom(new LearningNotification(toLearn, sourceEntity, 0.0001, 0, true, registry));
            lastPercentage = 0.0001;
        }

        @Override
        public void execute() {
            long curTime = System.currentTimeMillis();
            double completionPercent = (curTime - startTimestamp) / (double) (completionTimestamp - startTimestamp);
            if(completionPercent == 0) completionPercent = 0.0001;
            if (completionPercent < 1) {
                if(curTime >= lastUpdateTime + 5000) {
                    lastUpdateTime = curTime;
                    notifyEntityRoom(new LearningNotification(toLearn, sourceEntity, completionPercent, lastPercentage, !canceled, registry));
                    lastPercentage = completionPercent;
                }
            } else if (!canceled){
                boolean success = getSourceEntity().getSkills().learnSkill(toLearn);

                notifyEntityRoom(new LearningNotification(toLearn, sourceEntity, 1, lastPercentage, success, registry));
                controllerComplete = true;
                completionTimestamp = 0;
                pipeline.removeMessageContext(getSourceClient(),this);
            }else{
                notifyEntityRoom(new LearningNotification(toLearn, sourceEntity, 1, lastPercentage, false, registry));
                controllerComplete = true;
                completionTimestamp = 0;
                pipeline.removeMessageContext(getSourceClient(),this);
            }
        }

        @Override
        public boolean isComplete() {
            return controllerComplete || canceled;
        }

        @Override
        public long getTimeToExpire() {
            return completionTimestamp + 1000;
        }

        @Override
        public boolean registerMessage(Client sourceClient, String[] messageArgs) {
            if(messageArgs.length == 1 && "cease".equals(messageArgs[0])){
                sourceClient.sendMessage(getMessageInColor("You cease your channeling",WARNING));
                canceled = true;
            }else{
                sourceClient.sendMessage(getMessageInColor("Your concentration lies elsewhere. If you wish to stop channeling you must " + getMessageInColor("'cease'",INFORMATIVE) + " to do so",FAILURE));
            }
            return true;
        }
    }

    public class LearningNotification extends Notification {
        private Skill toLearn;
        private Entity learning;
        private double learnPercentage;
        private double lastPercentage;
        private boolean success;

        LearningNotification(Skill toLearn, Entity learning, double learnPercentage, double lastPercentage, boolean success, ClientRegistry registry) {
            super(registry);
            this.toLearn = toLearn;
            this.learning = learning;
            this.learnPercentage = learnPercentage;
            this.lastPercentage = lastPercentage;
            this.success = success;
        }

        @Override
        public String getAsMessage(Entity viewer) {
            if(!success){
                if (viewer.equals(learning))
                    return getMessageInColor("With a wrenching tear, the energy escapes your grasp. You fail to learn " + toLearn.getDisplayName() , FAILURE);
                else {
                    String skillName = toLearn.isVisibleToEntity(viewer) ? toLearn.getDisplayName() : "an unknown skill";
                    return getMessageInColor("With a pop and a tear the static around " + getEntityColored(learning, viewer, getWorldModel()) + " dissipates into nothing. " + learning.getPronoun() + " has failed to learn " + skillName, INFORMATIVE);
                }
            }else if (lastPercentage == 0) {
                int currentIP = (int) Math.floor(toLearn.getIPCost() * learnPercentage);
                if (viewer.equals(learning))
                    return getMessageInColor("You gather your information potential around you in a halo of " + ProgressionContainer.getIPBrightnessDescriptor(currentIP) + " static", INFORMATIVE);
                else
                    return getEntityColored(learning, viewer, getWorldModel()) + getMessageInColor(" gathers " + learning.getPossessivePronoun() + " information potential around " + learning.getReflexivePronoun() + " in a halo of " + ProgressionContainer.getIPBrightnessDescriptor(currentIP) + " static", INFORMATIVE);
            } else if (learnPercentage < 1) {
                int lastIP = (int) Math.floor(toLearn.getIPCost() * lastPercentage);
                String lastDescriptor = ProgressionContainer.getIPBrightnessDescriptor(lastIP);
                int currentIP = (int) Math.floor(toLearn.getIPCost() * learnPercentage);
                String currentDescriptor = ProgressionContainer.getIPBrightnessDescriptor(currentIP);

                String action = "";
                if (!currentDescriptor.equals(lastDescriptor))
                    action = " " + currentDescriptor;

                if (viewer.equals(learning))
                    return getMessageInColor("The light around you grows" + action, INFORMATIVE);
                else
                    return getMessageInColor("The light around " + getEntityColored(learning, viewer, getWorldModel()) + " grows" + action, INFORMATIVE);
            } else {
                String currentDescriptor = ProgressionContainer.getIPBrightnessDescriptor(toLearn.getIPCost());
                if (viewer.equals(learning))
                    return getMessageInColor("The energy around you crystallizes around you in a wreath of " + currentDescriptor + " static. As it sinks into your skin you learn " + toLearn.getDisplayName(), SUCCESS);
                else {
                    String skillName = toLearn.isVisibleToEntity(viewer) ? toLearn.getDisplayName() : "an unknown skill";
                    return getMessageInColor("There is an implosion of " + currentDescriptor + " static around " + getEntityColored(learning, viewer, getWorldModel()) + ". " + learning.getPronoun() + " has learned " + skillName, INFORMATIVE);
                }
            }
        }
    }
}
