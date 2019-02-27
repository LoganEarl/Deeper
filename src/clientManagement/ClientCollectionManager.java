package clientManagement;

import baseNetwork.WebServer;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Class holds a list of all clients and manages any requests they may make in a single thread in a synchronous matter.
 * Also responsible for managing the process of sending responses to client requests if needed. Maintains a WebServer object and
 * will eventually maintain a WorldSimulation object as well and will facilitate communication between the two.<br> <br>
 * Specifically, clients will be receiving messages and creating commands to be executed by their ClientCollectionManager.
 * @author Logan Earl
 */
//TODO this class is turning out to be a lot more complicated than i anticipated. Going to need to get eric in on this so we can uml it out
public class ClientCollectionManager {
    private Map<String, Client> clients;
    private WebServer server;
    //TODO needs a reference to the core database so it can access account info

    private Queue<ClientCommand> commandQueue = new LinkedList<>();

    private WebServer.OnMessageReceivedListener clientListener = new WebServer.OnMessageReceivedListener() {
        @Override
        public void onClientMessage(String client, WebServer.ClientMessage message) {
            if(!clients.containsKey(client))
                clients.put(client, new Client(ClientCollectionManager.this));
            /*TODO figure out what to do with the client message and how to designate that functionality.
                Chain of responsibility where we pass it to some sort of command parser? Or perhaps we just say fuck it
                and make client messages double as commands and save us the brain damage. For an example, we could make the
                ClientLoginMessage object just know to access the account database table and figure out if the client exists.
                If they do, set the client status to ACTIVE in and start sending status updates to character location and whatnot. If not
                then we gotta send them an account creation dialog message and update the client status to ACCOUNT_CREATION
            */
        }
    };



    public ClientCollectionManager(){
        /*TODO set up the web server and maybe start it up in the constructor? That feels wrong for some reason


         */
    }

    /**
     * Interface for a command that a client wants executed, such as a login attempt
     */
    public interface ClientCommand{
        void execute();
        void undo();
    }
}
