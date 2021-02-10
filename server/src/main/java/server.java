import org.w3c.dom.ls.LSOutput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class server {
    private final int PORT = 8889;
    private ServerSocket server;
    private Socket socket;
    private List<ClientHandler> clients;
    private AuthService authService;


    public server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started.");
            while (true) {
                socket = server.accept();
                System.out.println("Client connected" + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadCastMessage(ClientHandler sender, String message) {
        String clientMessage = String.format("[ %s ] : %s", sender.getNickname(), message);
        for (ClientHandler client : clients) {
            client.sendMessage(clientMessage);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }
}
