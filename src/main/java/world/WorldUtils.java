package main.java.world;

import main.java.client.Account;
import main.java.client.Client;
import main.java.world.entity.Entity;
import main.java.world.meta.World;

import java.util.Arrays;
import java.util.List;

public class WorldUtils {
    public static Entity getEntityOfClient(Client c, WorldModel model) {
        if (c.getStatus() != Client.ClientStatus.ACTIVE)
            return null;
        String entityID = c.getUserName();
        World w = World.getWorldOfEntityID(entityID);
        if (w != null)
            return model.getEntityCollection().getEntityByEntityID(entityID, w.getDatabaseName());
        return null;
    }

    public static String commaSeparate(List<?> args) {
        if (args.size() == 0)
            return "";
        else if (args.size() == 1)
            return args.get(0).toString();
        else if (args.size() == 2)
            return args.get(0).toString() + " and " + args.get(1).toString();
        else {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < args.size() - 1; i++)
                b.append(args.get(i).toString()).append(", ");
            b.append("and ").append(args.get(args.size() - 1).toString());
            return b.toString();
        }
    }

    public static boolean isAuthorized(Client sourceClient, Account.AccountType minimumType) {
        if (sourceClient != null && sourceClient.getAssociatedAccount() != null)
            return sourceClient.getAssociatedAccount().getAccountType().compareToAcountType(minimumType) >= 0;
        return false;
    }

    public static String getRefusedFlavorText() {
        String[] possibleRefuses = new String[]{
                "You muster your will, forging it, sculpting it into a great command to make the world tremble. You hurl it at space itself with your entire soul. Nothing happens. You feel as if you hear someone chuckling, perhaps you imagined it",

                "A man said to the universe: \n" +
                        "“Sir, I exist!”\n" +
                        "“However,” replied the universe, \n" +
                        "“The fact has not created in me \n" +
                        "A sense of obligation.” " +
                        "-Stephen Crane",

                "You find yourself incapable of doing that",

                "You feel as if you are lacking something"
        };


        int rnd = (int) (Math.random() * possibleRefuses.length);


        return possibleRefuses[rnd];
    }

    public static String commaSeparate(String... args) {
        return commaSeparate(Arrays.asList(args));
    }

    public static String commaSeparate(Object... args) {
        return commaSeparate(Arrays.asList(args));
    }
}
