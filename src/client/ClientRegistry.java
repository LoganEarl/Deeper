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
    private Map<String, Client> clients = new HashMap<>();
    private CommandExecutor commandExecutor;
    private WebServer localServer;
    private String accountDatabaseName;

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
    public Map<String, Client> getClients() {
        return clients;
    }

    /**
     * gets the client with the given internet address
     *
     * @param address the internet address of the client
     * @return either the client or null if not found
     */
    public Client getClient(String address) {
        return clients.get(address);
    }

    /**
     * creates a new client in the registry with the given address
     *
     * @param address the address of the new client
     */
    public void addClient(String address) {
        if (!clients.containsKey(address))
            clients.put(address, new Client(this, address));
    }

    /**
     * sends the given message in the form of a {@link PromptCommand} to all the given clients
     *
     * @param message         the message to send
     * @param clientAddresses the address(es) of the clients to receive the message
     */
    public void sendMessage(String message, String... clientAddresses) {
        commandExecutor.scheduleCommand(new PromptCommand(message, localServer, clientAddresses));
    }

    /**
     * sends the given message in the form of a {@link PromptCommand} to all the given clients
     *
     * @param message         the message to send
     * @param clientAddresses the address(es) of the clients to receive the message
     */
    public void sendMessage(String message, long messageTimestamp, String... clientAddresses) {
        commandExecutor.scheduleCommand(new PromptCommand(message, messageTimestamp, localServer, clientAddresses));
    }

    /**
     * sends the given message in the form of a {@link PromptCommand} to all the given clients
     *
     * @param message the message to send
     * @param clients the client(s) to receive the message
     */
    public void sendMessage(String message, Client... clients) {
        String[] addresses = new String[clients.length];
        for (int i = 0; i < clients.length; i++)
            addresses[i] = clients[i].getAddress();
        sendMessage(message, addresses);
    }

    public void sendMessage(String message, long sendTimestamp, Client... clients){
        String[] addresses = new String[clients.length];
        for (int i = 0; i < clients.length; i++)
            addresses[i] = clients[i].getAddress();
        sendMessage(message, sendTimestamp, addresses);
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
