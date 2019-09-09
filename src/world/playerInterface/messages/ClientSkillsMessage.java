package world.playerInterface.messages;

import client.Client;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.WorldModel;
import world.playerInterface.commands.SkillsCommand;

public class ClientSkillsMessage extends ClientMessage {
    public static final String HEADER = "skills";

    public ClientSkillsMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        return true;
    }

    @Override
    public String getUsage() {
        return "skills";
    }

    @Override
    public String getHelpText() {
        return "displays all skills. Some may be hidden until more prerequisites are met";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new SkillsCommand(getClient(),getWorldModel()));
    }
}
