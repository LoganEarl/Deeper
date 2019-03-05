package baseNetwork;

/**
 * Contains the types of messages that can be sent from clients. If any message type turns out to be un-parsable
 * the default value of UNKNOWN_MESSAGE_FORMAT will be used
 * @author Logan Earl
 */
public enum MessageType {
    /**Message sent from server to ask the client for information. ServerPromptMessage*/
    SERVER_PROMPT_MESSAGE("ServerPromptMessage"),


    /**message sent by the client to let the server know it is there. ClientGreeting*/
    CLIENT_GREETING("ClientGreeting"),
    /**Type used to denote an attempted login from a client. May or may not succeed. ClientLoginAttempt*/
    CLIENT_LOGIN_MESSAGE("ClientLoginAttempt"),
    /**Simple text message used for debugging purposes. DebugMessage*/
    CLIENT_DEBUG_MESSAGE("DebugMessage"),
    /**Message used to update account information. Also used to create new accounts. CLIENT_UPDATE_ACCOUNT_MESSAGE*/
    CLIENT_ACCOUNT_UPDATE_MESSAGE("CLIENT_UPDATE_ACCOUNT_MESSAGE"),
    /**Message sent from the client to elevate the permission level of a different user*/
    CLIENT_ELEVATE_USER_MESSAGE("ElevateUser"),


    /**The default type of messages that turned out to be un-parsable. UnknownMessageFormat*/
    UNKNOWN_MESSAGE_FORMAT("UnknownMessageFormat");

    private String messageType;

    MessageType(String parsableFormat) {
        this.messageType = parsableFormat;
    }

    public String getParsableFormat() {
        return messageType;
    }

    /**
     * Pass in the message type header from a client message and this method will attempt to determine the message type
     * @param toParse the message type from the message header
     * @return the type of the message or UNKNOWN_MESSAGE_FORMAT if it could not be determined
     */
    public MessageType parseFromString(String toParse) {
        for (MessageType m : MessageType.values())
            if (m.messageType.equals(toParse))
                return m;
        return UNKNOWN_MESSAGE_FORMAT;
    }
}
