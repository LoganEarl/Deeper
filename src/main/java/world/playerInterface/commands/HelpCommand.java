package main.java.world.playerInterface.commands;

import main.java.client.Client;
import main.java.network.CommandExecutor;
import main.java.network.messaging.MessagePipeline;

import java.util.Locale;
import java.util.Map;

public class HelpCommand implements CommandExecutor.Command {
    private String arg;
    private Client sourceClient;
    private MessagePipeline pipeline;
    private boolean complete = false;

    public HelpCommand(String arg, Client sourceClient, MessagePipeline pipeline){
        this.arg = arg;
        this.sourceClient = sourceClient;
        this.pipeline = pipeline;
    }

    @Override
    public void execute() {
        if(arg == null || arg.isEmpty())
            sourceClient.sendMessage(describeAllCommands());
        else
            sourceClient.sendMessage(describeCommand(arg));
        complete = true;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    private String describeCommand(String header){
        String usage = pipeline.getUsageForHeader(header);
        String help = pipeline.getHelpForHeader(header);

        return String.format(Locale.US,
                "Command: %s\nUsage: %s\n%s",
                header,usage,help);
    }

    private String describeAllCommands(){
        Map<String,String> usages = pipeline.getUsageMessages();
        StringBuilder response = new StringBuilder();

        for(String header: usages.keySet()){
            response.append(String.format(Locale.US,
                    "Command: %s\nUsage:%s\n\n",
                    header, usages.get(header)));
        }

        response.append("Use 'help help' to get more info");

        return response.toString();
    }
}
