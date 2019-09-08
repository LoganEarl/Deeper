package network;

import client.AccountTable;
import client.ClientRegistry;
import client.messages.*;
import database.DatabaseManager;
import network.messaging.MessagePipeline;
import world.WorldModel;
import world.meta.World;

import java.util.LinkedList;
import java.util.List;

/**
 * Class holds that holds it all together. Makes the server, the client registry, and the message pipeline. Then links them all up to run the server. Also loads the command classes into the message pipeline. All you have to do to run the server is make a command executor for the
 * server to attach to and pass it into the constructor with the port. Then call init(), then just keep right on calling the step() function
 * of the executor. That's it.
 *
 * @author Logan Earl
 */

public class SimulationManager {
    private WebServer server;
    private MessagePipeline messagePipeline;
    private CommandExecutor commandExecutor;
    private ClientRegistry clientRegistry;
    private WorldModel worldModel;

    private static final String DB_NAME = "account.db";

    /**
     * Sole constructor
     * @param port the internet port the server should run on
     * @param executor an executor for the server to attach to
     */
    public SimulationManager(int port, CommandExecutor executor) {
        this.commandExecutor = executor;

        server = new WebServer(port);
        clientRegistry = new ClientRegistry(executor, server,DB_NAME);
        worldModel = new WorldModel(executor,clientRegistry);
        messagePipeline = new MessagePipeline(clientRegistry,executor, worldModel);

        server.setMessageReceivedListener(messagePipeline);
        server.setClientRegistry(clientRegistry);
    }

    /**Starts the server and ensures the directory system and world system is all in place*/
    public void init() {
        List<DatabaseManager.DatabaseTable> tables = new LinkedList<>();
        tables.add(new AccountTable());

        DatabaseManager.createDirectories();
        DatabaseManager.createNewWorldDatabase(DB_NAME);
        DatabaseManager.createWorldTables(DB_NAME, tables);

        DatabaseManager.createDirectories();
        World.initWorldSystem();

        messagePipeline.loadMessage(ClientHelpMessage.class);
        messagePipeline.loadMessage(ClientLoginMessage.class);
        messagePipeline.loadMessage(ClientAccountUpdateMessage.class);
        messagePipeline.loadMessage(ClientDebugMessage.class);
        messagePipeline.loadMessage(ClientElevateUserMessage.class);
        messagePipeline.loadMessage(ClientLogoutMessage.class);
        messagePipeline.loadMessage(ClientRegisterMessage.class);

        worldModel.loadDefaultCommands(messagePipeline);
        worldModel.startDefaultTasks();

        server.startServer();
    }

    /**
     * gets the WebServer used to maintain client connections
     * @return the WebServer object
     */
    public WebServer getServer() {
        return this.server;
    }

    /**
     * gets the name of the database used to store client accounts
     * @return the string value of the database name including the file extension.
     */
    public String getDatabaseName(){
        return DB_NAME;
    }

}
