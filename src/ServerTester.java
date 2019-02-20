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
        private SimpleClientMessage(String message){
            this.message = message;
        }
        public byte[] getBytes(){
            return message.getBytes();
        }
        public String toString(){
            return message;
        }
    }
}
