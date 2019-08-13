package world.playerInterface.messages;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;
import network.messaging.ClientMessage;
import network.messaging.MessagePipeline;
import world.WorldModel;
import world.notification.NotificationService;
import world.playerInterface.commands.AttackCommand;

public class ClientAttackMessage extends ClientMessage {
    public static final String HEADER = "attack";

    private String target;

    public ClientAttackMessage(Client sourceClient, MessagePipeline messagePipeline, WorldModel worldModel) {
        super(HEADER, sourceClient, messagePipeline, worldModel);
    }

    @Override
    public boolean constructFromString(String rawMessage) {
        if(rawMessage.isEmpty())
            return false;

        target = rawMessage;
        return true;
    }

    @Override
    public String getUsage() {
        return "attack [your target]";
    }

    @Override
    public String getHelpText() {
        return "Attacking is done in two parts. First the attempt to hit, then the damage if the attack connects. The attempt to hit depends" +
                "on the item being used, as well as the armor of the defender. The defender can also be attempting to dodge the attack, which makes" +
                "hitting them much harder. Once a hit connects, the damage is calculated based on the weapon, the stats of the wielder, and the armor" +
                "of the defender. If the defender is deflecting the blow, damage is reduced depending on the stamina used in the deflect. There must" +
                "be a weapon in either the right or left hand of the attacker. The right hand is used first in the case of both. Note, two weapon " +
                "fighting is difficult, making scoring hits much harder without the proper training.\n" +
                "To Hit: weapon stat - (1-100) + weapon hit bonus - defender armor. Must be > 0 to hit";
    }

    @Override
    protected void doActions() {
        getWorldModel().getExecutor().scheduleCommand(new AttackCommand(target,getClient(), getWorldModel()));
    }
}
