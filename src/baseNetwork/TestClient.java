package baseNetwork;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

public class TestClient {
    private static final String MESSAGE_DIVIDER = "<!EOM!>";

    public static void main(String[] args) throws Exception{
        Socket socket = new Socket("localhost", 5555);
        BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        for(int i = 0; i < 10; i++) {
            out.write(("Hello Serer!" + MESSAGE_DIVIDER).getBytes());
            out.flush();
            Thread.sleep(1000);
        }
        out.close();
    }
}
