package network.messaging;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.WebServer;
import world.WorldModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagePipeline implements WebServer.OnMessageReceivedListener {
    private Map<String, Constructor<? extends ClientMessage>> loadedMessageBuilders = new HashMap<>();
    private Map<String, String> usageMessages = new HashMap<>();
    private Map<String, String> helpMessages = new HashMap<>();
    private Map<Client, List<MessageContext>> specificContexts = new HashMap<>();
    private ClientRegistry registry;
    private CommandExecutor executor;
    private WorldModel model;

    public MessagePipeline(ClientRegistry registry, CommandExecutor executor, WorldModel model) {
        this.registry = registry;
        this.executor = executor;
        this.model = model;
    }

    @Override
    public void onClientMessage(Client client, String rawMessage) {
        if (specificContexts.containsKey(client)) {
            List<MessageContext> contexts = specificContexts.get(client);
            if(contexts != null) {
                for (MessageContext context : contexts)
                    if (context.registerMessage(client, rawMessage.split("\n")))
                        return;
            }
        }

        String header = getHeader(rawMessage);

        if (!header.isEmpty() && loadedMessageBuilders.containsKey(header)) {
            try {
                ClientMessage message = loadedMessageBuilders.get(header).newInstance(client, this, model);

                if (message.constructFromString(getMessageBody(rawMessage)))
                    message.resolve();
            } catch (Exception e) {
                System.out.println("Attempt to invoke a clientMessage constructor failed");
                e.printStackTrace();
            }

        } else
            registry.sendMessage("Im sorry, but I did not recognize that. Please try again or use 'help' if you have any questions", client);
    }

    public void loadMessage(Class<? extends ClientMessage> messageClass) {
        try {
            Field headerField = messageClass.getField("HEADER");
            if (Modifier.isStatic(headerField.getModifiers()) && Modifier.isFinal(headerField.getModifiers())) {
                String header = (String) headerField.get(null);
                if (loadedMessageBuilders.containsKey(header))
                    System.out.println("Unable to load class " + messageClass.getName() + " as it's header " + header + " is already bound to a different message");
                Constructor<? extends ClientMessage> messageConstructor = ClientMessage.getConstructor(messageClass);
                if (messageConstructor != null) {
                    ClientMessage tempInstance = messageConstructor.newInstance(null,null,null);
                    usageMessages.put(header,tempInstance.getUsage());
                    helpMessages.put(header,tempInstance.getHelpText());
                    loadedMessageBuilders.put(header, messageConstructor);
                }else
                    System.out.println("Unable to load message" + messageClass.getName() + " because it has no appropriate constructor");
            }
        } catch (Exception e) {
            System.out.println("Unable to load message" + messageClass.getName() + " because it has no static final header field or a related exception");
            e.printStackTrace();
        }
    }

    private String getHeader(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty())
            return "";
        String[] args = rawMessage.split("\n");
        if (args.length >= 1)
            return args[0];
        else
            return "";
    }

    private String getMessageBody(String rawMessage) {
        int firstIndex = rawMessage.indexOf("\n");
        if (firstIndex > -1 && firstIndex < rawMessage.length())
            return rawMessage.substring(firstIndex + 1);
        return "";
    }

    public void addMessageContext(Client client, MessageContext messageContext) {
        specificContexts.computeIfAbsent(client, k -> new ArrayList<>(2));
        if(!specificContexts.get(client).contains(messageContext)) specificContexts.get(client).add(messageContext);
    }

    public void removeMessageContext(Client client, MessageContext messageContext){
        if(specificContexts.containsKey(client) && specificContexts.get(client) != null){
            specificContexts.get(client).remove(messageContext);
        }
    }

    public Map<String,String> getUsageMessages(){
        return usageMessages;
    }

    public Map<String,String> getHelpMessages(){
        return helpMessages;
    }

    public String getUsageForHeader(String header){
        if(usageMessages.containsKey(header))
            return usageMessages.get(header);
        return header;
    }

    public String getHelpForHeader(String header){
        if(helpMessages.containsKey(header))
            return helpMessages.get(header);
        return "";
    }

    /**
     * a custom context in which commands can be executed. Messages that arrive from the
     * associated entity will first be routed to the context. The context can then choose
     * to consume the message or ignore it. If it is ignored, the message will go to the
     * default context for all world messages.
     */
    public interface MessageContext {
        /**
         * @return the time in milliseconds until the context should self-invalidate to prevent memory leaks
         */
        long getTimeToExpire();

        /**
         * called when a message is received from the associated entity
         *
         * @param sourceClient the client that sent the message
         * @param messageArgs  the message that was sent
         * @return true to consume the message, false if the message should continue on to other contexts
         */
        boolean registerMessage(Client sourceClient, String[] messageArgs);
    }
}
