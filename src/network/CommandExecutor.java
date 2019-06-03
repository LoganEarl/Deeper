package network;

import java.util.LinkedList;
import java.util.Queue;

public class CommandExecutor {
    private Queue<Command> commandQueue = new LinkedList<>();
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

    /**
     * Interface for a command that a client wants executed, such as a login attempt
     */
    public interface Command {
        /**Called by the server thread when executing the command.*/
        void execute();

        /**
         * Called by the server thread after executing the command. Determines if the command was consumed by the last execute call.
         * If so, it will not be called again and removed from the command queue. If not, it will continue to be executed
         * @return true to consume the command
         */
        boolean isComplete();
    }

}
