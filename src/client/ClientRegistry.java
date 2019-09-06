package client;

import client.commands.PromptCommand;
import network.CommandExecutor;
import network.WebServer;

import java.util.HashMap;
import java.util.Map;

/**
 * A class used to keep track of the clients that are active on the server and each of their authentication statuses.
 *
 * @author Logan Eark
 */
public class ClientRegistry {
    private Map<Long, Client> clients = new HashMap<>();
    private CommandExecutor commandExecutor;
    private WebServer localServer;
    private String accountDatabaseName;

    private long lastID = 0;

    /**
     * Sole constructor
     *
     * @param commandExecutor     executor used to send message commands
     * @param localServer         the server the clients are connected too
     * @param accountDatabaseName the name of the database file containing account info
     */
    public ClientRegistry(CommandExecutor commandExecutor, WebServer localServer, String accountDatabaseName) {
        this.commandExecutor = commandExecutor;
        this.localServer = localServer;
        this.accountDatabaseName = accountDatabaseName;
    }

    /**
     * gets the client with the given username if they are currently logged in
     *
     * @param username the username of the client
     * @return the client if found, null otherwise
     */
    public Client getClientWithUsername(String username) {
        if (username == null)
            throw new NullPointerException("Cannot get client with null username");
        for (Client client : clients.values()) {
            if (client.getAssociatedAccount() != null &&
                    client.getAssociatedAccount().getUserName().equals(username))
                return client;
        }
        return null;
    }

    /**
     * gets all clients mapped by their internet addresses
     *
     * @return a Map of the clients where the key is their ip address and the value is the client
     */
    public Map<Long, Client> getClients() {
        return clients;
    }


    public Client createClient(String address){
        long newID = lastID + 1;

        Client newClient = new Client(this,address,newID);
        clients.put(newID,newClient);

        lastID = newID;

        return newClient;
    }

    /**
     * gets the client with the given internet address
     *
     * @param id the identifier of the client
     * @return either the client or null if not found
     */
    public Client getClient(long id) {
        return clients.get(id);
    }

    /**
     * sends the given message in the form of a {@link PromptCommand} to all the given clients
     *
     * @param message         the message to send
     * @param clients the client(s) to receive the message
     */
    public void sendMessage(String message, Client... clients) {
        commandExecutor.scheduleCommand(new PromptCommand(message, localServer, clients));
    }

    /**
     * sends the given message in the form of a {@link PromptCommand} to all the given clients
     *
     * @param message         the message to send
     * @param clients the address(es) of the clients to receive the message
     */
    public void sendMessage(String message, long messageTimestamp, Client... clients) {
        commandExecutor.scheduleCommand(new PromptCommand(message, messageTimestamp, localServer, clients));
    }

    public void disconnect(Client... toDisconnect){
        localServer.disconnectClients(toDisconnect);
        for(Client c: toDisconnect)
            clients.remove(c.getIdentifier());
    }

    /**
     * get the name of the database file containing account info
     *
     * @return the name of the file with account info
     */
    public String getDatabaseName() {
        return accountDatabaseName;
    }

    /**
     * schedules the given command on the executor used by the server
     *
     * @param c the command to schedule
     */
    public void scheduleOnSharedExecutor(CommandExecutor.Command c) {
        commandExecutor.scheduleCommand(c);
    }
}
