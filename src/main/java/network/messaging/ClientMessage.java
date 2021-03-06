package main.java.network.messaging;

import main.java.client.Client;
import main.java.network.CommandExecutor;
import main.java.world.WorldModel;

import java.lang.reflect.Constructor;

public abstract class ClientMessage {
    private String signifier;
    private Client sourceClient;
    private MessagePipeline messagePipeline;
    private WorldModel worldModel;

    public ClientMessage(String messageSignifier,
                            Client sourceClient,
                            MessagePipeline messagePipeline,
                            WorldModel worldModel){
        this.signifier = messageSignifier;
        this.sourceClient = sourceClient;
        this.messagePipeline = messagePipeline;
        this.worldModel = worldModel;
    }

    public WorldModel getWorldModel() {
        return worldModel;
    }

    /**the first word of this message*/
    public final String getMessageSignifier(){
        return signifier;
    }

    public final Client getClient(){
        return sourceClient;
    }

    public final MessagePipeline getMessagePipeline(){
        return messagePipeline;
    }

    public abstract boolean constructFromString(String rawMessage);

    public abstract String getUsage();

    public abstract String getHelpText();

    public final void resolve(){
        if(worldModel.getExecutor() != null) {
            worldModel.getExecutor().scheduleCommand(new CommandExecutor.Command() {
                private boolean complete = false;

                @Override
                public void execute() {
                    doActions();
                    complete = true;
                }

                @Override
                public boolean isComplete() {
                    return complete;
                }
            });
        }
    }

    /**will start any actions needed to complete this message*/
    protected abstract void doActions();

    public static Constructor<? extends ClientMessage> getConstructor(Class<? extends  ClientMessage> messageClass){
        try {
            return messageClass.getConstructor(Client.class, MessagePipeline.class, WorldModel.class);
        }catch (Exception e){
            return null;
        }
    }
}