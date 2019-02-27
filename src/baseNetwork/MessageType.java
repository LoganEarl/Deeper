package baseNetwork;

/**
 * Contains the types of messages that can be sent from clients. If any message type turns out to be un-parsable
 * the default value of UNKNOWN_MESSAGE_FORMAT will be used
 * @author Logan Earl
 */
public enum MessageType {
    /**Type used to denote an attempted login from a client. May or may not succeed*/
    LOGIN_MESSAGE("ClientLoginAttempt"),
    /**Simple text message used for debugging purposes*/
    DEBUG_MESSAGE("DebugMessage"),
    /**The default type of messages that turned out to be un-parsable*/
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
