package network.messaging;

import client.Client;
import network.CommandExecutor;

import java.lang.reflect.Constructor;

public abstract class ClientMessage {
    private String signifier;
    private CommandExecutor executor;
    private Client sourceClient;

    protected ClientMessage(String messageSignifier, Client sourceClient, CommandExecutor executor){
        this.signifier = messageSignifier;
        this.executor = executor;
        this.sourceClient = sourceClient;
    }

    /**the first word of this message*/
    public final String getMessageSignifier(){
        return signifier;
    }

    public final Client getClient(){
        return sourceClient;
    }

    public abstract boolean constructFromString(String rawMessage);

    public final void resolve(){
        executor.scheduleCommand(new CommandExecutor.Command() {
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

    /**will start any actions needed to complete this message*/
    protected abstract void doActions();

    public static Constructor<? extends ClientMessage> getConstructor(Class<? extends  ClientMessage> messageClass){
        try {
            return messageClass.getConstructor();
        }catch (Exception e){
            return null;
        }
    }
}