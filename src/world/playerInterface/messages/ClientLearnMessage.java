package world.playerInterface.messages;

import client.Client;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.WorldModel;
import world.playerInterface.commands.LearnSkillCommand;

public class ClientLearnMessage extends ClientMessage {
    public static final String HEADER = "learn";

    private String skillName;

    public ClientLearnMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        if(rawMessage.isEmpty())
            return false;

        skillName = rawMessage;
        return true;
    }

    @Override
    public String getUsage() {
        return "learn [skill name]";
    }

    @Override
    public String getHelpText() {
        return "Used to learn new skills. Not only must you be aware of the skill, but you must have the required Information Potential (IP) to construct the skill. Information Theory makes learning new things faster, but the price must still be paid";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new LearnSkillCommand(skillName,getClient(),getMessagePipeline(),getWorldModel()));
    }
}
