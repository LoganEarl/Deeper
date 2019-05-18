package world.playerInterface;

import network.WebServer;

public enum WorldMessageType implements WebServer.MessageType {
    CLIENT_INVENTORY_MESSAGE("inventory"),
    CLIENT_STATS_MESSAGE("stats"),
    CLIENT_ATTACK_MESSAGE("attack"),
    CLIENT_HOLD_MESSAGE("grab"),
    CLIENT_LOOK_MESSAGE("look"),
    /**The default type of messages that turned out to be un-parsable. UnknownMessageFormat*/
    UNKNOWN_MESSAGE_FORMAT("UnknownMessageFormat");

    private String messageType;

    WorldMessageType(String parsableFormat) {
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
    public static WorldMessageType parseFromString(String toParse) {
        for (WorldMessageType m : WorldMessageType.values())
            if (m.messageType.equals(toParse.toLowerCase()))
                return m;
        return UNKNOWN_MESSAGE_FORMAT;
    }
}
