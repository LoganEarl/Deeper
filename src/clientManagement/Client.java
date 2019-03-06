package clientManagement;

import baseNetwork.MessageType;
import baseNetwork.SimulationManager;
import baseNetwork.WebServer;
import clientManagement.clientMessages.ClientAccountUpdateMessage;
import clientManagement.clientMessages.ClientLoginMessage;
import clientManagement.commands.PromptCommand;

/**
 * Class that provides the link between an account and a client connection
 * @author Logan Earl
 */
public class Client {
    private SimulationManager provider;
    private ClientStatus status;
    private Account associatedAccount;
    private String address;

    public Client(SimulationManager provider, String address){
        this.provider = provider;
        this.status = ClientStatus.UNAUTHENTICATED;
        this.address = address;
        provider.scheduleCommand(new PromptCommand(
                "Hello, Welcome to the project.\n" +
                "Please use the (login 'username' 'password') or the (register 'username' 'password' 'email' commands to login or register respectively.",
                provider.getServer(), address));
    }

    public void setStatus(ClientStatus newStatus){
        this.status = newStatus;
    }

    public void registerMessage(WebServer.ClientMessage message){
        if(message.getMessageType() == MessageType.CLIENT_LOGIN_MESSAGE && status == ClientStatus.UNAUTHENTICATED){
            tryLogIn((ClientLoginMessage) message);
        }
        if(message.getMessageType() == MessageType.CLIENT_ACCOUNT_UPDATE_MESSAGE &&
                (status == ClientStatus.UNAUTHENTICATED || status == ClientStatus.ACTIVE)){
            tryUpdateInfo((ClientAccountUpdateMessage) message);
        }
    }

    private void tryLogIn(final ClientLoginMessage message){
        provider.scheduleCommand(new SimulationManager.Command() {
            boolean complete = false;
            @Override
            public void execute() {
                Account myAccount = Account.getAccountByUsername(message.getUserName(), provider.getDatabaseName());
                if(myAccount == null || !myAccount.checkPassword(message.getHashedPassword()))
                    provider.scheduleCommand(new PromptCommand("Unknown Username/Password. Please try again", provider.getServer(), message.getClient()));
                else{
                    associatedAccount = myAccount;
                    status = ClientStatus.ACTIVE;
                    provider.scheduleCommand(new PromptCommand("Success, welcome " + associatedAccount.getUserName(), provider.getServer(), message.getClient()));
                }
                complete = true;
            }

            @Override
            public boolean isComplete() {
                return complete;
            }
        });
    }

    private void tryUpdateInfo(ClientAccountUpdateMessage message){
        provider.scheduleCommand(new SimulationManager.Command() {
            boolean complete = false;
            @Override
            public void execute() {
                Account attemptedNewUser = Account.getAccountByUsername(message.getNewUserName(),provider.getDatabaseName());
                //they are creating a new account
                if(attemptedNewUser == null && status == ClientStatus.UNAUTHENTICATED &&
                        associatedAccount == null && message.getOldHashedPassword().isEmpty() && message.getOldUserName().isEmpty()) {
                    Account newAccount = new Account(message.getNewUserName(), message.getNewHashedPassword(), message.getNewEmailAddress(), Account.AccountType.BASIC);
                    newAccount.saveToDatabase(provider.getDatabaseName());
                    associatedAccount = newAccount;
                //they are not logged in
                } else if(associatedAccount == null || status == ClientStatus.INACTIVE || status == ClientStatus.UNAUTHENTICATED){
                    provider.scheduleCommand(new PromptCommand("Unknown Username/Password. Please try again",
                            provider.getServer(), message.getClient()));
                //they are trying the change name to existing name
                }else if(attemptedNewUser != null && !associatedAccount.getUserName().equals(attemptedNewUser.getUserName())){
                    provider.scheduleCommand(new PromptCommand("That username is already taken. Please try again",
                            provider.getServer(), message.getClient()));
                //they are logged in and updating info
                }else if(associatedAccount.checkPassword(message.getOldHashedPassword())){
                    associatedAccount.setUserName(message.getNewUserName());
                    associatedAccount.setHashedPassword(message.getNewHashedPassword());
                    if(!message.getNewEmailAddress().isEmpty())
                        associatedAccount.setEmail(message.getNewEmailAddress());
                    associatedAccount.updateInDatabase(provider.getDatabaseName());
                }
                complete = true;
            }

            @Override
            public boolean isComplete() {
                return complete;
            }
        });
    }

    public String getAddress(){
        return this.address;
    }

    /**Enumeration of possible client states*/
    public enum ClientStatus{
        /**client has either logged out or was kicked at some point. Has no open connections associated with client account*/
        INACTIVE,
        /**client has just connected. We have an internet address but client has not yet provided any login info so we don't know who it is yet*/
        UNAUTHENTICATED,
        /**client is both logged in and active, free to interact with the system normally*/
        ACTIVE
    }
}
