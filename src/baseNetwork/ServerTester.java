package baseNetwork;

public class ServerTester {
    public static void main(String[] args) {
        WebServer server = new WebServer(5555, listener, parser);
        server.startServer();
        System.out.println("Started Server");
    }

    private static WebServer.OnMessageReceivedListener listener = (client, message) -> System.out.println("Client " + client + " says: " + message.toString());

    private static WebServer.ClientMessageParser parser = SimpleClientMessage::new;

    private static class SimpleClientMessage implements WebServer.ClientMessage{
        private String message;
        private String sourceClient;

        SimpleClientMessage(String message, String sourceClient) {
            this.message = message;
            this.sourceClient = sourceClient;
        }

        public String toString(){
            return message;
        }

        @Override
        public String getMessageType() {
            return "SimpleTextMessage";
        }

        @Override
        public String getClient() {
            return sourceClient;
        }
    }
}
