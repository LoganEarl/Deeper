package network;

import client.Client;

public interface ClientMessage {
    String getMessageSignifier();
    Client getClient();
    boolean constructFromString(String rawMessage);
}