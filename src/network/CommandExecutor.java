package network;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CommandExecutor {
    private List<Command> commandQueue = Collections.synchronizedList(new LinkedList<>());
    /**
     * preforms a single simulation step by executing all scheduled commands and re-enqueueing all commands that are not yet complete
     */
    public void step() {
        List<Command> continuingCommands = new LinkedList<>();

        while(commandQueue.size() > 0){
            Command c = commandQueue.remove(0);

            if(c.getStartTimestamp() <= System.currentTimeMillis())
                c.execute();
            if (!c.isComplete())
                continuingCommands.add(c);
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

    /**
     * Interface for a command that a client wants executed, such as a login attempt
     */
    public interface Command {
        /**Called by the server thread when executing the command.*/
        void execute();

        default long getStartTimestamp(){
            return 0;
        }
        /**
         * Called by the server thread after executing the command. Determines if the command was consumed by the last execute call.
         * If so, it will not be called again and removed from the command queue. If not, it will continue to be executed
         * @return true to consume the command
         */
        boolean isComplete();
    }

}
