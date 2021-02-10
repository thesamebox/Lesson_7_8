import commands.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private server server;
    private Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    private String nickname;

    public ClientHandler(server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(()-> {
                try {
                    //логгирование
                    while (true) {
                        String tryToAuth = in.readUTF();
                        if (tryToAuth.startsWith("/")) {
                            if (tryToAuth.equals(Command.END)) {
                                System.out.println("client was disconnected");
                                out.writeUTF(Command.END);
                                throw new RuntimeException("server disconnected us");
                            }
                            if (tryToAuth.startsWith(Command.AUTH)) {
                                String[] token = tryToAuth.split("\\s");
                                String newNick = server.getAuthService().GetNicknameByLogAndPass(token[1], token[2]);
                                if (newNick != null) {
                                    nickname = newNick;
                                    sendMessage(Command.AUTH_OK + " " + nickname);
                                    server.subscribe(this);
                                    server.broadCastMessage(this, " connected");
                                    break;
                                } else {
                                    sendMessage("Wrong login / password");
                                }
                            }
                        }
                    }
                    // общение
                    while (true) {
                        String clientMessage = in.readUTF();
                        if (clientMessage.startsWith("/")) {
                            if (clientMessage.equals(Command.END)) {
                                out.writeUTF(Command.END);
                                break;
                            }
                        } else {
                            server.broadCastMessage(this, clientMessage);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    server.broadCastMessage(this," disconnected");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

}
