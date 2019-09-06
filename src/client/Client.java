package client;

import java.util.Locale;
import static world.playerInterface.ColorTheme.*;

/**
 * Class that provides the link between an account and a client connection
 *
 * @author Logan Earl
 */
public class Client {
    private ClientStatus status;
    private Account associatedAccount;
    private String address;
    private long identifier;
    private ClientRegistry clientRegistry;

    public Client(ClientRegistry clientRegistry, String address, long identifier) {
        this.status = ClientStatus.UNAUTHENTICATED;
        this.clientRegistry = clientRegistry;
        this.address = address;
        this.identifier = identifier;
        this.clientRegistry.sendMessage("Hello, Welcome to the project.\n" +
                getMessageInColor("Please use the login or register commands to proceed.",INFORMATIVE)  + " If you have any questions on command usage, use 'help [your command here]' or just 'help' to get more info", this);
    }

    public void setStatus(ClientStatus newStatus) {
        this.status = newStatus;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void tryLogIn(final Client sourceClient, final String userName, final String hPass) {
        Account myAccount = Account.getAccountByUsername(userName, clientRegistry.getDatabaseName());
        if (myAccount == null || !myAccount.checkPassword(hPass))
            clientRegistry.sendMessage(getMessageInColor("Unknown Username/Password. Please try again",FAILURE), sourceClient);
        else {
            Client preexisting = clientRegistry.getClient(myAccount);
            if(preexisting != null) {
                preexisting.status = ClientStatus.UNAUTHENTICATED;
                preexisting.associatedAccount = null;
                preexisting.sendMessage(getMessageInColor("You have logged in from another location. You are now logged out",FAILURE));
            }

            associatedAccount = myAccount;
            status = ClientStatus.ACTIVE;
            clientRegistry.sendMessage(getMessageInColor("Success, welcome " + associatedAccount.getUserName(),SUCCESS), sourceClient);
        }
    }

    public void tryLogOut(Client sourceClient, String targetUsername) {
        if (targetUsername.isEmpty()) {
            status = ClientStatus.UNAUTHENTICATED;
            associatedAccount = null;
            clientRegistry.sendMessage(getMessageInColor("You have been logged out",INFORMATIVE), sourceClient);
        } else {
            Client targetedClient = clientRegistry.getClient(targetUsername);
            if (targetedClient == null) {
                clientRegistry.sendMessage(getMessageInColor("There is no client logged in with that name",FAILURE), sourceClient);
            } else if (associatedAccount.getAccountType().compareToAcountType(targetedClient.associatedAccount.getAccountType()) <= 0) {
                clientRegistry.sendMessage(getMessageInColor("You cannot kick " + associatedAccount.getUserName() + " as they have greater or equal privileges to yourself",FAILURE), sourceClient);
            } else {
                clientRegistry.sendMessage(getMessageInColor("Got em', you have kicked " + targetedClient.associatedAccount.getUserName() + " from the server",SUCCESS), sourceClient);
                clientRegistry.sendMessage(getMessageInColor("Oof, you have been kicked by " + associatedAccount.getUserName(),FAILURE), targetedClient);
                targetedClient.status = ClientStatus.UNAUTHENTICATED;
                targetedClient.associatedAccount = null;
            }
        }

    }

    public void tryUpdateInfo(Client sourceClient, String oldUser, String newUser, String oldHPass, String newHPass, String newEmail) {
        Account attemptedNewUser = Account.getAccountByUsername(newUser, clientRegistry.getDatabaseName());
        //they are creating a new account
        if (attemptedNewUser == null && status == ClientStatus.UNAUTHENTICATED &&
                associatedAccount == null && oldHPass.isEmpty() && oldUser.isEmpty()) {
            Account newAccount = new Account(newUser, newHPass, newEmail, Account.AccountType.BASIC);
            newAccount.saveToDatabase(clientRegistry.getDatabaseName());
            associatedAccount = newAccount;
            clientRegistry.sendMessage(getMessageInColor("Success, new account created",SUCCESS), sourceClient);
            //they are not logged in
        } else if (associatedAccount == null || status == ClientStatus.INACTIVE || status == ClientStatus.UNAUTHENTICATED) {
            clientRegistry.sendMessage(getMessageInColor("Unknown Username/Password. Please try again",FAILURE),
                    sourceClient);
            //they are trying the change name to existing name
        } else if (attemptedNewUser != null && !associatedAccount.getUserName().equals(attemptedNewUser.getUserName())) {
            clientRegistry.sendMessage(getMessageInColor("That username is already taken. Please try again",FAILURE),
                    sourceClient);
            //they are logged in and updating info
        } else if (associatedAccount.checkPassword(oldHPass)) {
            associatedAccount.setUserName(newUser);
            associatedAccount.setHashedPassword(newHPass);
            if (!newEmail.isEmpty())
                associatedAccount.setEmail(newEmail);
            associatedAccount.updateInDatabase(clientRegistry.getDatabaseName());
            clientRegistry.sendMessage(getMessageInColor("Success. Account information has been updated",SUCCESS),
                    sourceClient);
        } else {
            clientRegistry.sendMessage(getMessageInColor("Unable to update info, old UserName/Password combination does not match any users",FAILURE),
                    sourceClient);
        }
    }


    public void tryElevateUser(final Client sourceClient, final String targetUserName, final Account.AccountType newAccountType) {
        Account userToElevate = Account.getAccountByUsername(targetUserName, clientRegistry.getDatabaseName());
        if (userToElevate == null) {
            clientRegistry.sendMessage(getMessageInColor("Unable to find user " + targetUserName,FAILURE),
                    sourceClient);
        } else if (associatedAccount.getAccountType() == Account.AccountType.GOD) {
            userToElevate.setAccountType(newAccountType);
            userToElevate.updateInDatabase(clientRegistry.getDatabaseName());
            clientRegistry.sendMessage(String.format(Locale.US, getMessageInColor("Oh powerful one, you have changed %s's permission level to %d",SUCCESS),
                    targetUserName, newAccountType.getSavableForm()),
                    sourceClient);
        } else {
            if (associatedAccount.getAccountType().compareToAcountType(userToElevate.getAccountType()) <= 0) {
                clientRegistry.sendMessage(getMessageInColor("Cannot change permissions of user greater than yourself",FAILURE),
                        sourceClient);
            } else if (associatedAccount.getAccountType().compareToAcountType(newAccountType) <= 0) {
                clientRegistry.sendMessage(getMessageInColor("Cannot assign privileges to user greater than or equal to your own",FAILURE),
                        sourceClient);
            } else {
                userToElevate.setAccountType(newAccountType);
                userToElevate.updateInDatabase(clientRegistry.getDatabaseName());
                clientRegistry.sendMessage(String.format(Locale.US, getMessageInColor("You have changed %s's permission level to %d",SUCCESS),
                        targetUserName, newAccountType.getSavableForm()),
                        sourceClient);
            }
        }
    }

    public String getAddress() {
        return this.address;
    }

    public String getUserName() {
        if (this.associatedAccount != null)
            return associatedAccount.getUserName();
        return null;
    }

    public long getIdentifier() {
        return identifier;
    }

    public void sendMessage(String message){
        clientRegistry.sendMessage(message,this);
    }

    public void sendMessage(String message, long messageTimestamp){
        clientRegistry.sendMessage(message,messageTimestamp,this);
    }

    public Account getAssociatedAccount() {
        return associatedAccount;
    }

    /**
     * Enumeration of possible client states
     */
    public enum ClientStatus {
        /**
         * client has either logged out or was kicked at some point. Has no open connections associated with client account
         */
        INACTIVE,
        /**
         * client has just connected. We have an internet address but client has not yet provided any login info so we don't know who it is yet
         */
        UNAUTHENTICATED,
        /**
         * client is both logged in and active, free to interact with the system normally
         */
        ACTIVE
    }
}
