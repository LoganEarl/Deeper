package network;

import client.Account;
import client.AccountTable;
import client.Client;
import client.commands.PromptCommand;
import client.messages.*;
import database.DatabaseManager;
import world.meta.World;
import world.playerInterface.PlayerManagementInterface;
import world.playerInterface.WorldMessageParser;

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
    private Map<String, Client> clients = new HashMap<>();
    private WebServer server;
    private PlayerManagementInterface service;

    private static final String DB_NAME = "account.db";
    //TODO needs a reference to the core database so it can access account info

    private Queue<Command> commandQueue = new LinkedList<>();

    private final WebServer.OnMessageReceivedListener clientListener = (client, message) -> {
        if(!message.wasCorrectlyParsed())
            scheduleCommand(new PromptCommand("Unable to parse command", server, client));
        else {
            if (!clients.containsKey(client)) {
                clients.put(client, new Client(SimulationManager.this, client));
            }
            if(!clients.get(client).registerMessage(message) && !service.registerMessage(message))
                scheduleCommand(new PromptCommand("Unable to process that command at this time",server,client));
        }
    };

    public SimulationManager(int port) {
        server = new WebServer(port, clientListener);
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

    /**
     * preforms a single simulation step by executing all scheduled commands and re-enqueueing all commands that are not yet complete
     */
    public void step() {
        Queue<Command> toExecute = commandQueue;
        Queue<Command> continuingCommands = new LinkedList<>();
        commandQueue = new LinkedList<>();

        for (Command c : toExecute) {
            c.execute();
            if (!c.isComplete()) continuingCommands.add(c);
        }
        commandQueue.addAll(continuingCommands);
    }

    /**
     * schedule a command to be executed next simulation step
     *
     * @param c the command to be executed
     */
    public void scheduleCommand(Command c) {
        commandQueue.add(c);
    }

    public WebServer getServer() {
        return this.server;
    }

    public String getDatabaseName(){
        return DB_NAME;
    }

    public Map<String,Client> getClients(){
        return clients;
    }

    public Client getClientWithAddress(String address){
        return clients.get(address);
    }

    public void notifyOfLoginComplete(Account a, Client c){
        service.registerEntityControlSource(c.getAddress(),a.getUserName());
    }

    /**
     * Interface for a command that a client wants executed, such as a login attempt
     */
    public interface Command {
        /**Called by the server thread when executing the command.*/
        void execute();

        /**
         * Called by the server thread after executing the command. Determines if the command was consumed by the last execute call.
         * If so, it will not be called again and removed from the command queue. If not, it will continue to be executed
         * @return true to consume the command
         */
        boolean isComplete();
    }
}
