package client.commands;

import client.Client;
import client.ClientRegistry;
import network.CommandExecutor;

public class DisconnectCommand implements CommandExecutor.Command {
    private boolean complete = false;
    private Client toDisconnect;
    private ClientRegistry registry;

    public DisconnectCommand(Client toDisconnect, ClientRegistry registry) {
        this.toDisconnect = toDisconnect;
        this.registry = registry;
    }

    @Override
    public void execute() {
        registry.disconnect(toDisconnect);
        complete = true;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }
}
