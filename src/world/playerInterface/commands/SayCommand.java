package world.playerInterface.commands;

import client.Client;
import client.ClientRegistry;
import world.WorldModel;
import world.entity.Entity;
import world.notification.Notification;
import world.notification.NotificationSubscriber;

import static world.playerInterface.ColorTheme.*;

public class SayCommand extends EntityCommand {
    boolean complete = false;
    private ClientRegistry registry;
    private String message;

    public SayCommand(String message, Client sourceClient, WorldModel model){
        super(sourceClient, model);
        this.registry = model.getRegistry();
        this.message = message;
    }

    @Override
    protected void executeEntityCommand() {
        notifyEntityRoom(new SpeechNotification(getSourceEntity(),message, registry));

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

    public class SpeechNotification extends Notification{
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
            if(viewer.equals(sourceEntity))
                message = getMessageInColor( "You say " + toSay, INFORMATIVE);
            else
                message = getEntityColored(viewer,sourceEntity,getWorldModel()) + getMessageInColor( " says " + toSay, INFORMATIVE);

            return message;
        }
    }
}
