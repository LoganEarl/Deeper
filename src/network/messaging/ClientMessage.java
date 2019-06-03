package network.messaging;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;

import java.lang.reflect.Constructor;

public abstract class ClientMessage {
    private String signifier;
    private CommandExecutor executor;
    private Client sourceClient;
    private ClientRegistry hostRegistry;
    private MessagePipeline messagePipeline;

    protected ClientMessage(String messageSignifier,
                            Client sourceClient,
                            CommandExecutor executor,
                            ClientRegistry registry,
                            MessagePipeline messagePipeline){
        this.signifier = messageSignifier;
        this.executor = executor;
        this.sourceClient = sourceClient;
        this.hostRegistry = registry;
        this.messagePipeline = messagePipeline;
    }

    /**the first word of this message*/
    public final String getMessageSignifier(){
        return signifier;
    }

    public final Client getClient(){
        return sourceClient;
    }

    public final CommandExecutor getExecutor(){
        return executor;
    }

    public final ClientRegistry getClientRegistry(){
        return hostRegistry;
    }

    public final MessagePipeline getMessagePipeline(){
        return messagePipeline;
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
            return messageClass.getConstructor(Client.class,CommandExecutor.class, ClientRegistry.class, MessagePipeline.class);
        }catch (Exception e){
            return null;
        }
    }
}