package network;

import client.AccountTable;
import client.ClientRegistry;
import client.messages.*;
import database.DatabaseManager;
import network.messaging.MessagePipeline;
import world.meta.World;
import world.playerInterface.messages.ClientCreateCharacterMessage;
import world.playerInterface.messages.ClientLookMessage;

import java.util.*;

/**
 * Class holds a list of all clients and manages any requests they may make in a single thread in a synchronous matter.
 * Also responsible for managing the process of sending responses to client requests if needed. Maintains a WebServer object and
 * will eventually maintain a WorldSimulation object as well and will facilitate communication between the two.<br> <br>
 * Specifically, clients will be receiving messages and creating commands to be executed by their network.SimulationManager.
 *
 * @author Logan Earl
 */

public class SimulationManager {
    private WebServer server;
    private MessagePipeline messagePipeline;
    private CommandExecutor commandExecutor;
    private ClientRegistry clientRegistry;

    private static final String DB_NAME = "account.db";
    //TODO needs a reference to the core database so it can access account info

    public SimulationManager(int port, CommandExecutor executor) {
        this.commandExecutor = executor;

        server = new WebServer(port);
        clientRegistry = new ClientRegistry(executor, server,DB_NAME);
        messagePipeline = new MessagePipeline(clientRegistry,executor);

        server.setMessageRecievedListener(messagePipeline);
    }

    public void init() {
        List<DatabaseManager.DatabaseTable> tables = new LinkedList<>();
        tables.add(new AccountTable());

        DatabaseManager.createDirectories();
        DatabaseManager.createNewWorldDatabase(DB_NAME);
        DatabaseManager.createWorldTables(DB_NAME, tables);

        DatabaseManager.createDirectories();
        World.initWorldSystem();

        messagePipeline.loadMessage(ClientLoginMessage.class);
        messagePipeline.loadMessage(ClientAccountUpdateMessage.class);
        messagePipeline.loadMessage(ClientDebugMessage.class);
        messagePipeline.loadMessage(ClientElevateUserMessage.class);
        messagePipeline.loadMessage(ClientLogoutMessage.class);

        messagePipeline.loadMessage(ClientCreateCharacterMessage.class);
        messagePipeline.loadMessage(ClientLookMessage.class);


        server.startServer();
    }

    public WebServer getServer() {
        return this.server;
    }

    public String getDatabaseName(){
        return DB_NAME;
    }

}
