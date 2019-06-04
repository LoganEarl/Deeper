package world;

import client.Client;
import world.entity.Entity;
import world.meta.World;

import java.util.Arrays;
import java.util.List;

public class WorldUtils {
    public static Entity getEntityOfClient(Client c){
        if(c.getStatus() != Client.ClientStatus.ACTIVE)
            return null;
        String entityID = c.getUserName();
        World w = World.getWorldOfEntityID(entityID);
        if(w != null)
            return Entity.getEntityByEntityID(entityID,w.getDatabaseName());
        return null;
    }

    public static String commaSeparate(List args){
        if(args.size() == 0)
            return "";
        else if(args.size() == 1)
            return args.get(0).toString();
        else if(args.size() == 2)
            return args.get(0).toString() + " and " + args.get(1).toString();
        else{
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < args.size()-1; i++)
                b.append(args.get(i).toString()).append(", ");
            b.append("and ").append(args.get(args.size()-1).toString());
            return b.toString();
        }
    }

    public static String commaSeparate(String... args){
        return commaSeparate(Arrays.asList(args));
    }

    public static String commaSeparate(Object... args){
        return commaSeparate(Arrays.asList(args));
    }
}
