package network;

import client.AccountTable;
import client.ClientRegistry;
import database.DatabaseManager;
import network.messaging.MessagePipeline;
import world.meta.World;
import world.playerInterface.PlayerManagementInterface;

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
    private PlayerManagementInterface service;
    private MessagePipeline messagePipeline;
    private CommandExecutor commandExecutor;
    private ClientRegistry clientRegistry;

    private static final String DB_NAME = "account.db";
    //TODO needs a reference to the core database so it can access account info

    public SimulationManager(int port, CommandExecutor executor) {
        clientRegistry = new ClientRegistry()
        messagePipeline = new MessagePipeline();
        this.commandExecutor = executor;
        server = new WebServer(port, messagePipeline);
        service = new PlayerManagementInterface(this);
    }

    public void init() {
        List<DatabaseManager.DatabaseTable> tables = new LinkedList<>();
        tables.add(new AccountTable());

        DatabaseManager.createDirectories();
        DatabaseManager.createNewWorldDatabase(DB_NAME);
        DatabaseManager.createWorldTables(DB_NAME, tables);

        DatabaseManager.createDirectories();
        World.initWorldSystem();

        server.startServer();
    }

    public WebServer getServer() {
        return this.server;
    }

    public String getDatabaseName(){
        return DB_NAME;
    }

}
