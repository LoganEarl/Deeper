package world.playerInterface.messages;

import client.Account;
import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.meta.World;
import world.notification.NotificationService;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public class ClientViewWorldMessage extends ClientMessage {
    public static final String HEADER = "view";

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String viewArg = "";

    public ClientViewWorldMessage(Client sourceClient, CommandExecutor executor, ClientRegistry registry, MessagePipeline messagePipeline, NotificationService notificationService) {
        super(HEADER, sourceClient, executor, registry, messagePipeline, notificationService);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        String[] args = rawMessage.split("\n");
        if (args.length == 1 && args[0].equals("worlds")) {
            return true;
        }
        if (args.length == 2 && args[0].equals("world") && !args[1].isEmpty()) {
            viewArg = args[1];
            return true;
        }
        return false;
    }

    @Override
    protected void doActions() {
        if (getClient().getAssociatedAccount() != null &&
                getClient().getAssociatedAccount().getAccountType().compareToAcountType(Account.AccountType.MODERATOR) >= 0) {
            if (viewArg.isEmpty()) {
                getClient().sendMessage(getAllWorldsString());
            } else {
                try {
                    World toView = World.getWorldByWorldID(Integer.parseInt(viewArg));
                    getClient().sendMessage(getWorldTitleString() + getWorldString(toView));
                } catch (Exception e) {
                    getClient().sendMessage("Unable to find world by id: " + viewArg);
                }
            }
        } else
            getClient().sendMessage("You must be a moderator to do that");
    }

    @Override
    public String getUsage() {
        return "{view worlds/view world [world id]}";
    }

    @Override
    public String getHelpText() {
        return "The greatest of auguries see only a hint of what you do, oh esteemed one. You can look into the very threads of creation, seeing what was, what is, and what will be";
    }

    private String getAllWorldsString() {
        Collection<World> allWorlds = World.getAllWorlds();
        StringBuilder worldText = new StringBuilder();
        worldText.append(getWorldTitleString());

        for (World w : allWorlds) {
            worldText.append(getWorldString(w)).append("\n");
        }
        return worldText.toString();
    }

    private String getWorldTitleString() {
        return String.format(Locale.US, "%16.16s    %8.8s    %8.8s    %10.10s    %16.16s    %16.16s    %16.16s\n",
                "World Name", "World ID", "Status", "Size", "Start Time", "End Time", "Duration");
    }

    private String getWorldString(World w) {
        String startTime = "N/A";
        String endTime = "N/A";
        String duration = "";
        long start = w.getStartTime();
        long end = w.getEndTime();
        long durationMinutes = w.getDurationMinutes();
        duration = String.format("%dY %dD %dH %dM",durationMinutes/60/24/365, durationMinutes/60/24 % 365, durationMinutes/60 % 24, durationMinutes % 60);
        Date startDate = new Date(start);
        Date endDate = new Date(end);

        if (start != 0)
            startTime = formatter.format(startDate);
        if (end != 0)
            endTime = formatter.format(endDate);

        return (String.format(Locale.US, "%16.16s    %8.8s    %8.8s    %10d    %16.16s    %16.16s    %16.16s",
                w.getName(), w.getWorldID(), w.getStatus(), w.getPortalSize(), startTime, endTime, duration));
    }
}
