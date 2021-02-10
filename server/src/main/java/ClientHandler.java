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
    private String login;

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
                                login = token[1];
                                if (newNick != null) {
                                    if (!server.isLoginAuthorized(login)) {
                                        nickname = newNick;
                                        sendMessage(Command.AUTH_OK + " " + nickname);
                                        server.subscribe(this);
                                        server.broadCastMessage(this, " connected");
                                        break;
                                    } else {
                                        sendMessage("The login has been authorized");
                                    }
                                } else {
                                    sendMessage("Wrong login / password");
                                }
                            }
                            if (tryToAuth.startsWith(Command.REGISTRATION)) {
                                String[] token = tryToAuth.split("\\s");
                                if (token.length < 4) {
                                    continue;
                                }
                                boolean regSuccessful = server.getAuthService().registration(token[1], token[2], token[3]);
                                if (regSuccessful) {
                                    sendMessage(Command.REG_OK);
                                } else {
                                    sendMessage(Command.REG_FAIl);
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
                            if (clientMessage.startsWith(Command.WHISPER)) {
                                String[] token = clientMessage.split("\\s", 3);
                                if (token.length < 3) {
                                    sendMessage("Input the command like  \"/w nickname p_message\"");
                                } else {
                                    server.privateMessage(this, token[1], token[2]);
                                }
                            }
                        } else {
                            server.broadCastMessage(this, clientMessage);
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.broadCastMessage(this," disconnected");
                    server.unsubscribe(this);
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

    public String getLogin() {
        return login;
    }
}
