package client;

import client.commands.PromptCommand;
import client.messages.ClientAccountUpdateMessage;
import client.messages.ClientElevateUserMessage;
import client.messages.ClientLoginMessage;
import client.messages.ClientLogoutMessage;
import network.MessageType;
import network.SimulationManager;
import network.WebServer;

import java.util.Locale;
import java.util.Map;

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

    public boolean registerMessage(WebServer.ClientMessage message){
        boolean handledMessage = false;
        if(message.getMessageType() == MessageType.CLIENT_LOGIN_MESSAGE){
            tryLogIn((ClientLoginMessage) message);
            handledMessage = true;
        }else if(message.getMessageType() == MessageType.CLIENT_ACCOUNT_UPDATE_MESSAGE &&
                (status == ClientStatus.UNAUTHENTICATED || status == ClientStatus.ACTIVE)){
            tryUpdateInfo((ClientAccountUpdateMessage) message);
            handledMessage = true;
        }else if(message.getMessageType() == MessageType.CLIENT_ELEVATE_USER_MESSAGE &&
                (status == ClientStatus.ACTIVE && associatedAccount != null)){
            tryElevateUser((ClientElevateUserMessage)message);
            handledMessage = true;
        }else if(message.getMessageType() == MessageType.CLIENT_LOGOUT_MESSAGE){
            tryLogOut((ClientLogoutMessage)message);
            handledMessage = true;
        }
        return handledMessage;
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

    private void tryLogOut(ClientLogoutMessage message){
        provider.scheduleCommand(new SimulationManager.Command() {
            private boolean isComplete = false;
            @Override
            public void execute() {
                if(message.getTargetUserName().isEmpty()){
                    status = ClientStatus.UNAUTHENTICATED;
                    associatedAccount = null;
                    provider.scheduleCommand(new PromptCommand("You have been logged out",provider.getServer(),message.getClient()));
                }else{
                    Client targetedClient = null;
                    Map<String,Client> connectedClients = provider.getClients();
                    for(Client c: connectedClients.values()){
                        if(c.associatedAccount != null && c.associatedAccount.getUserName().equals(message.getTargetUserName()))
                            targetedClient = c;
                    }
                    if(targetedClient == null){
                        provider.scheduleCommand(new PromptCommand("There is no client logged in with that name",provider.getServer(),message.getClient()));
                    }else if(associatedAccount.getAccountType().compareToAcountType(targetedClient.associatedAccount.getAccountType()) <= 0){
                        provider.scheduleCommand(new PromptCommand("You cannot kick " + associatedAccount.getUserName() + " as they have greater or equal privileges to yourself",provider.getServer(),message.getClient()));
                    }else{
                        provider.scheduleCommand(new PromptCommand("Got em', you have kicked " + targetedClient.associatedAccount.getUserName() + " from the server",provider.getServer(),message.getClient()));
                        provider.scheduleCommand(new PromptCommand("Ouch, you have been kicked by " + associatedAccount.getUserName(),provider.getServer(),targetedClient.address));
                        targetedClient.status = ClientStatus.UNAUTHENTICATED;
                        targetedClient.associatedAccount = null;
                    }
                }
                isComplete = true;
            }

            @Override
            public boolean isComplete() {
                return isComplete;
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
                    provider.scheduleCommand(new PromptCommand("Success, new account created", provider.getServer(),message.getClient()));
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
                    provider.scheduleCommand(new PromptCommand("Success. Account information has been updated",
                            provider.getServer(),message.getClient()));
                }else{
                    provider.scheduleCommand(new PromptCommand("Unable to update info, old UserName/Password combination does not match any users",
                            provider.getServer(),message.getClient()));
                }
                complete = true;
            }

            @Override
            public boolean isComplete() {
                return complete;
            }
        });
    }

    private void tryElevateUser(ClientElevateUserMessage message){
        provider.scheduleCommand(new SimulationManager.Command() {
            private boolean complete = false;
            @Override
            public void execute() {
                Account userToElevate = Account.getAccountByUsername(message.getTargetUserName(),provider.getDatabaseName());
                if(userToElevate == null){
                    provider.scheduleCommand(new PromptCommand("Unable to find user " + message.getTargetUserName(),
                            provider.getServer(),message.getClient()));
                } else if (associatedAccount.getAccountType() == Account.AccountType.GOD){
                    userToElevate.setAccountType(message.getNewAccountType());
                    userToElevate.updateInDatabase(provider.getDatabaseName());
                    provider.scheduleCommand(new PromptCommand(String.format(Locale.US,"Oh powerful one, you have changed %s's permission level to %d",
                            message.getTargetUserName(),message.getNewAccountType().getSavableForm()),
                            provider.getServer(),message.getClient()));
                }else {
                    if (associatedAccount.getAccountType().compareToAcountType(userToElevate.getAccountType()) <= 0) {
                        provider.scheduleCommand(new PromptCommand("Cannot change permissions of user greater than yourself",
                                provider.getServer(), message.getClient()));
                    } else if (associatedAccount.getAccountType().compareToAcountType(message.getNewAccountType()) <= 0) {
                        provider.scheduleCommand(new PromptCommand("Cannot assign privileges to user greater than or equal to your own",
                                provider.getServer(), message.getClient()));
                    } else {
                        userToElevate.setAccountType(message.getNewAccountType());
                        userToElevate.updateInDatabase(provider.getDatabaseName());
                        provider.scheduleCommand(new PromptCommand(String.format(Locale.US,"You have changed %s's permission level to %d",
                                message.getTargetUserName(),message.getNewAccountType().getSavableForm()),
                                provider.getServer(),message.getClient()));
                    }
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
