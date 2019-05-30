package world.playerInterface.messages;

import network.WebServer;
import world.playerInterface.WorldMessageType;

public class ContextMessage implements WebServer.ClientMessage  {
    private String messageHeader;
    private String sourceClient;
    private String[] fullArgs;
    private boolean parsed = false;

    public ContextMessage(String sourceClient, String rawMessageHeader){
        this.sourceClient = sourceClient;
        this.messageHeader = rawMessageHeader;
    }

    @Override
    public WebServer.MessageType getMessageType() {
        return WorldMessageType.UNKNOWN_MESSAGE_FORMAT;
    }

    @Override
    public String getClient() {
        return null;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
        String[] rawArgs = rawMessageBody.split("\n");
        if(rawArgs.length >= 1 && !rawArgs[0].isEmpty()) {
            fullArgs = new String[rawArgs.length + 1];
            fullArgs[0] = messageHeader;
            for(int i = 0; i < rawArgs.length; i++)
                fullArgs[i+1] = rawArgs[i];
        }
        fullArgs = new String[]{messageHeader};
        parsed = true;
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return parsed;
    }

    public String[] getArgs(){
        return fullArgs;
    }
}
