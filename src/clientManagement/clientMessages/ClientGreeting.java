package clientManagement.clientMessages;

public class ClientGreeting implements WebServer.ClientMessage{
    private String client;

    public ClientGreeting(String sourceClient){
        this.client = sourceClient;
    }

    public String getMessage() {
        return "Hello Server, I am Client:" + client;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CLIENT_GREETING;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public void constructFromString(String rawMessageBody) {
    }

    @Override
    public boolean wasCorrectlyParsed() {
        return true;
    }
}
