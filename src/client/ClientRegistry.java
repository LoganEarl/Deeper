package client;

import client.commands.PromptCommand;
import network.CommandExecutor;
import network.WebServer;

import java.util.HashMap;
import java.util.Map;

public class ClientRegistry {
    private Map<String, Client> clients = new HashMap<>();
    private CommandExecutor commandExecutor;
    private WebServer localServer;
    private String accountDatabaseName;

    public ClientRegistry(CommandExecutor commandExecutor, WebServer localServer, String accountDatabaseName){
        this.commandExecutor = commandExecutor;
        this.localServer = localServer;
        this.accountDatabaseName = accountDatabaseName;
    }

    public Client getClientWithUsername(String username){
        if(username == null)
            throw new NullPointerException("Cannot get client with null username");
        for(Client client:clients.values()){
            if(client.getAssociatedAccount() != null &&
                    client.getAssociatedAccount().getUserName().equals(username))
                return client;
        }
        return null;
    }

    public Map<String,Client> getClients(){
        return clients;
    }

    public Client getClient(String address){
        return clients.get(address);
    }


    public void addClient(String address){
        if(!clients.containsKey(address))
            clients.put(address, new Client(this, address));
    }

    public void sendMessage(String message, String... clientAddresses){
        commandExecutor.scheduleCommand(new PromptCommand(message,localServer, clientAddresses));
    }

    public void sendMessage(String message, Client... clients){
        String[] addresses = new String[clients.length];
        for(int i = 0; i < clients.length; i++)
            addresses[i] = clients[i].getAddress();
        sendMessage(message, addresses);
    }

    public String getDatabaseName(){
        return accountDatabaseName;
    }

    public void scheduleOnSharedExecutor(CommandExecutor.Command c){
        commandExecutor.scheduleCommand(c);
    }
}
