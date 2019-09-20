package main.java.client.messages;

import main.java.client.Client;
import main.java.network.messaging.ClientMessage;
import main.java.network.messaging.MessagePipeline;
import main.java.world.WorldModel;
import main.java.world.playerInterface.commands.HelpCommand;

public class ClientHelpMessage extends ClientMessage {
    public static final String HEADER = "help";

    private String arg = "";

    public ClientHelpMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if(args.length == 0 || (args.length == 1 && args[0].isEmpty())){
            return true;
        }
        if(args.length == 1){
            arg = args[0];
            return true;
        }
        return false;
    }

    @Override
    public String getUsage() {
        return "help ([command])";
    }

    @Override
    public String getHelpText() {
        return "This command is used to get help on the usage of commands. \nThere are several symbols that are used in usage text\n\n" +
                "[]: used to denote that the text inside is paraphrasing what the content should be\n" +
                "(): used to denote that everything inside is optional and can be safely left out\n" +
                "{}: used to denote that the text inside represents a limited collection of options, one of which must be used\n" +
                "EXAMPLES:\n" +
                "   help ([command]) indicates that you can either type 'help', 'help go', 'help look' or on any other command\n" +
                "   login [username] [password] indicates that you must type the word 'login' every time, but your username and password\n" +
                "       change and are up to your discretion. You must however, type something in for both for the command to work\n" +
                "   look ({in/at} [item or container name close by]) indicates that you must type 'look'. You may or may not decide to continue.\n" +
                "       If you do, the format must be 'look' followed by either 'in' or 'at' followed by either an item or a container you\n" +
                "       are close to. 'look', 'look at Joe' 'look in box' are all examples of this format";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new HelpCommand(arg,getClient(),getMessagePipeline()));
    }
}
