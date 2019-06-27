package client;

import java.util.Locale;

/**
 * Class that provides the link between an account and a client connection
 *
 * @author Logan Earl
 */
public class Client {
    private ClientStatus status;
    private Account associatedAccount;
    private String address;
    private ClientRegistry clientRegistry;

    public Client(ClientRegistry clientRegistry, String address) {
        this.status = ClientStatus.UNAUTHENTICATED;
        this.clientRegistry = clientRegistry;
        this.address = address;
        this.clientRegistry.sendMessage("Hello, Welcome to the project.\n" +
                "Please use the (login 'username' 'password') or the (register 'username' 'password' 'email' commands to login or register respectively.", address);
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
            clientRegistry.sendMessage("Unknown Username/Password. Please try again", sourceClient);
        else {
            associatedAccount = myAccount;
            status = ClientStatus.ACTIVE;
            clientRegistry.sendMessage("Success, welcome " + associatedAccount.getUserName(), sourceClient);
        }
    }

    public void tryLogOut(Client sourceClient, String targetUsername) {
        if (targetUsername.isEmpty()) {
            status = ClientStatus.UNAUTHENTICATED;
            associatedAccount = null;
            clientRegistry.sendMessage("You have been logged out", sourceClient);
        } else {
            Client targetedClient = clientRegistry.getClientWithUsername(targetUsername);
            if (targetedClient == null) {
                clientRegistry.sendMessage("There is no client logged in with that name", sourceClient);
            } else if (associatedAccount.getAccountType().compareToAcountType(targetedClient.associatedAccount.getAccountType()) <= 0) {
                clientRegistry.sendMessage("You cannot kick " + associatedAccount.getUserName() + " as they have greater or equal privileges to yourself", sourceClient);
            } else {
                clientRegistry.sendMessage("Got em', you have kicked " + targetedClient.associatedAccount.getUserName() + " from the server", sourceClient);
                clientRegistry.sendMessage("Oof, you have been kicked by " + associatedAccount.getUserName(), targetedClient.address);
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
            clientRegistry.sendMessage("Success, new account created", sourceClient.getAddress());
            //they are not logged in
        } else if (associatedAccount == null || status == ClientStatus.INACTIVE || status == ClientStatus.UNAUTHENTICATED) {
            clientRegistry.sendMessage("Unknown Username/Password. Please try again",
                    sourceClient.getAddress());
            //they are trying the change name to existing name
        } else if (attemptedNewUser != null && !associatedAccount.getUserName().equals(attemptedNewUser.getUserName())) {
            clientRegistry.sendMessage("That username is already taken. Please try again",
                    sourceClient.getAddress());
            //they are logged in and updating info
        } else if (associatedAccount.checkPassword(oldHPass)) {
            associatedAccount.setUserName(newUser);
            associatedAccount.setHashedPassword(newHPass);
            if (!newEmail.isEmpty())
                associatedAccount.setEmail(newEmail);
            associatedAccount.updateInDatabase(clientRegistry.getDatabaseName());
            clientRegistry.sendMessage("Success. Account information has been updated",
                    sourceClient.getAddress());
        } else {
            clientRegistry.sendMessage("Unable to update info, old UserName/Password combination does not match any users",
                    sourceClient.getAddress());
        }
    }


    public void tryElevateUser(final Client sourceClient, final String targetUserName, final Account.AccountType newAccountType) {
        Account userToElevate = Account.getAccountByUsername(targetUserName, clientRegistry.getDatabaseName());
        if (userToElevate == null) {
            clientRegistry.sendMessage("Unable to find user " + targetUserName,
                    sourceClient.getAddress());
        } else if (associatedAccount.getAccountType() == Account.AccountType.GOD) {
            userToElevate.setAccountType(newAccountType);
            userToElevate.updateInDatabase(clientRegistry.getDatabaseName());
            clientRegistry.sendMessage(String.format(Locale.US, "Oh powerful one, you have changed %s's permission level to %d",
                    targetUserName, newAccountType.getSavableForm()),
                    sourceClient.getAddress());
        } else {
            if (associatedAccount.getAccountType().compareToAcountType(userToElevate.getAccountType()) <= 0) {
                clientRegistry.sendMessage("Cannot change permissions of user greater than yourself",
                        sourceClient.getAddress());
            } else if (associatedAccount.getAccountType().compareToAcountType(newAccountType) <= 0) {
                clientRegistry.sendMessage("Cannot assign privileges to user greater than or equal to your own",
                        sourceClient.getAddress());
            } else {
                userToElevate.setAccountType(newAccountType);
                userToElevate.updateInDatabase(clientRegistry.getDatabaseName());
                clientRegistry.sendMessage(String.format(Locale.US, "You have changed %s's permission level to %d",
                        targetUserName, newAccountType.getSavableForm()),
                        sourceClient.getAddress());
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
