import commands.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;


public class server {
    private final int PORT = 8889;
    private ServerSocket server;
    private Socket socket;
    private List<ClientHandler> clients;
    private AuthServiceSQLite authService;
    private ExecutorService executorService;
    private final int ThreadsPool = 50;

    private final Logger logger = Logger.getLogger(server.class.getName());

    public server() throws SQLException, ClassNotFoundException {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthServiceSQLite();
        try {
            Handler h = new FileHandler("Chat.log");
            h.setFormatter(new SimpleFormatter());
            logger.addHandler(h);
            executorService = Executors.newFixedThreadPool(ThreadsPool);
            server = new ServerSocket(PORT);
            logger.log(Level.FINE, "The server is started");
//            System.out.println("Server started.");
            while (true) {
                socket = server.accept();
                logger.log(Level.INFO, "Client connected" + socket.getRemoteSocketAddress());
                System.out.println("Client connected" + socket.getRemoteSocketAddress());
                executorService.execute(new ClientHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Something went wrong", e);
        } finally {
            logger.log(Level.OFF, "The server is shut down");
            executorService.shutdown();
        }
    }

    public void broadCastMessage(ClientHandler sender, String message) {
        String clientMessage = String.format("[ %s ] : %s", sender.getNickname(), message);
        for (ClientHandler client : clients) {
            client.sendMessage(clientMessage);
            logger.log(Level.FINE, "Client " + sender + " send a message");
        }
    }

    public void privateMessage(ClientHandler senderNickname, String receiverNickname, String message) {
        String privateMessageFrom = String.format("Whisper from [ %s ] : %s", senderNickname.getNickname(), message);
        String privateMessageTo = String.format("Whisper to [ %s ] : %s", receiverNickname, message);
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(receiverNickname)) {
                client.sendMessage(privateMessageFrom);
                if (!client.equals(senderNickname)) {
                    senderNickname.sendMessage(privateMessageTo);
                    logger.log(Level.FINE, "Client " + senderNickname + " send a personal message to " + receiverNickname);
                }
                return;
            }
        }
        senderNickname.sendMessage("User \"" + receiverNickname + "\" is not found");
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        logger.log(Level.FINE, "Client " + clientHandler + " joined to the chatroom");

        broadCastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        logger.log(Level.FINE, "Client " + clientHandler + " joined to the chatroom");
        broadCastClientList();
    }

    public AuthServiceSQLite getAuthService() {
        return authService;
    }

    public boolean isLoginAuthorized(String login) {
        for (ClientHandler client : clients) {
            if (client.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadCastClientList() {
        StringBuilder nameLists = new StringBuilder(Command.CLIENT_LIST);
        for (ClientHandler client : clients) {
            nameLists.append(" ").append(client.getNickname());
        }
        String list = nameLists.toString();
        for (ClientHandler client : clients) {
            client.sendMessage(list);
        }
    }
}
