package baseNetwork;

import clientManagement.Client;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Class holds a list of all clients and manages any requests they may make in a single thread in a synchronous matter.
 * Also responsible for managing the process of sending responses to client requests if needed. Maintains a WebServer object and
 * will eventually maintain a WorldSimulation object as well and will facilitate communication between the two.<br> <br>
 * Specifically, clients will be receiving messages and creating commands to be executed by their baseNetwork.SimulationManager.
 *
 * @author Logan Earl
 */

public class SimulationManager {
    private Map<String, Client> clients;
    private WebServer server;
    //TODO needs a reference to the core database so it can access account info

    private Queue<Command> commandQueue = new LinkedList<>();

    private final WebServer.OnMessageReceivedListener clientListener = (client, message) -> {
        if (!clients.containsKey(client))
            clients.put(client, new Client(SimulationManager.this, client));
            /*TODO figure out what to do with the client message and how to designate that functionality.
                Chain of responsibility where we pass it to some sort of command parser? Or perhaps we just say fuck it
                and make client messages double as commands and save us the brain damage. For an example, we could make the
                ClientLoginMessage object just know to access the account database table and figure out if the client exists.
                If they do, set the client status to ACTIVE in and start sending status updates to character location and whatnot. If not
                then we gotta send them an account creation dialog message and update the client status to ACCOUNT_CREATION
            */
    };

    private final WebServer.ClientMessageParser clientParser = (toParse, sourceClient) -> {
        int headerLastIndex = toParse.indexOf('\n');
        if(headerLastIndex == -1 || headerLastIndex == toParse.length()-1)
            return null;

        String rawMessageType = toParse.substring(0, headerLastIndex);
        String rawMessageBody = toParse.substring(headerLastIndex+1);
        MessageType messageType = MessageType.valueOf(rawMessageType);
        switch (messageType){
            case CLIENT_GREETING:

                break;

            case CLIENT_DEBUG_MESSAGE:

                break;
        }

        return null;
    };

    public SimulationManager(int port) {
        server = new WebServer(port, clientListener, clientParser);

        /*TODO set up the web server and maybe start it up in the constructor? That feels wrong for some reason


         */
    }

    public void init() {
        server.startServer();
    }

    /**
     * preforms a single simulation step by executing all scheduled commands and re-enqueueing all commands that are not yet complete
     */
    public void step() {
        Queue<Command> toExecute = commandQueue;
        Queue<Command> continuingCommands = new LinkedList<>();
        commandQueue = new LinkedList<>();

        for (Command c : toExecute) {
            c.execute();
            if (!c.isComplete()) continuingCommands.add(c);
        }
        commandQueue.addAll(continuingCommands);
    }

    /**
     * schedule a command to be executed next simulation step
     *
     * @param c the command to be executed
     */
    public void scheduleCommand(Command c) {
        commandQueue.add(c);
    }

    public WebServer getServer() {
        return this.server;
    }

    /**
     * Interface for a command that a client wants executed, such as a login attempt
     */
    public interface Command {
        void execute();

        boolean isComplete();
    }
}
