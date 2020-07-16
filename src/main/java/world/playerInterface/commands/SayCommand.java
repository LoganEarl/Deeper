package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.client.ClientRegistry;
import main.java.world.WorldModel;
import main.java.world.entity.Entity;
import main.java.world.notification.ConcreteNotification;

import static main.java.world.playerInterface.ColorTheme.*;

public class SayCommand extends EntityCommand {
    boolean complete = false;
    private ClientRegistry registry;
    private String message;

    public SayCommand(String message, Client sourceClient, WorldModel model) {
        super(sourceClient, model);
        this.registry = model.getRegistry();
        this.message = message;
    }

    @Override
    protected void executeEntityCommand() {
        notifyEntityRoom(new SpeechNotification(getSourceEntity(), message, registry));

        complete = true;
    }

    @Override
    protected boolean canDoWhenDying() {
        return true;
    }

    @Override
    protected boolean requiresBalance() {
        return false;
    }

    @Override
    public boolean entityCommandIsComplete() {
        return complete;
    }

    public class SpeechNotification extends ConcreteNotification {
        private Entity sourceEntity;
        private String toSay;

        private SpeechNotification(Entity sourceEntity, String message, ClientRegistry registry) {
            super(registry);

            this.sourceEntity = sourceEntity;
            this.toSay = message;
        }

        @Override
        public String getAsMessage(Entity viewer) {
            String message;
            if (viewer.equals(sourceEntity))
                message = getMessageInColor("You say " + toSay, INFORMATIVE);
            else
                message = getEntityColored(sourceEntity, viewer, getWorldModel()) + getMessageInColor(" says " + toSay, INFORMATIVE);

            return message;
        }
    }
}
