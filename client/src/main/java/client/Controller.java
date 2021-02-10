package client;

import commands.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    @FXML
    private HBox authPanel;
    @FXML
    private HBox messagePanel;
    @FXML
    private Button ButtonSend;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField loginField;
    @FXML
    private Button authorize;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;

    private Stage stage;

    private static Socket socket;
    private static final int PORT = 8889;
    private static final String IP_ADDRESS = "localhost";

    private static DataInputStream in;
    private static DataOutputStream out;

    private boolean isAuth;

    private String nickname;

    private void setAuth(boolean auth) {
        this.isAuth = auth;
        messagePanel.setVisible(isAuth);
        messagePanel.setManaged(isAuth);
        authPanel.setVisible(!isAuth);
        authPanel.setManaged(!isAuth);
        if (!isAuth) {
            nickname = "";
        }
        setTitle(nickname);
        textArea.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        Platform.runLater(() -> textField.requestFocus());
        Platform.runLater(()-> {
            stage = (Stage) textField.getScene().getWindow();
        });
        setAuth(false);

    }

    private void connect () {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // логгирование
                    while (true) {
                        String clientMessage = in.readUTF();
                        if (clientMessage.startsWith("/")) {
                            if (clientMessage.equals(Command.END)) {
                                System.out.println("server disconnected us");
                                throw new RuntimeException("server disconnected us");
                            }
                            if (clientMessage.startsWith(Command.AUTH_OK)) {
                                nickname = clientMessage.split("\\s")[1];
                                setAuth(true);
                                break;
                            }

                        } else {
                            textArea.appendText(clientMessage + "\n");
                        }
                    }
                    // общение
                    while (true) {
                        String clientMessage = in.readUTF();
                        if (clientMessage.equals(Command.END)) {
                            System.out.println("Disconnected");
                            break;
                        }
                        textArea.appendText(clientMessage + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuth(false);
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

    @FXML
    private void clickBtnSend(ActionEvent actionEvent) {
        try {
            if (textField.getText().trim().length() > 0) {
                out.writeUTF(textField.getText());
                textField.clear();
                textField.requestFocus();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void tryToAuthorization(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String accessMessage = String.format("%s %s %s", Command.AUTH, loginField.getText().trim(), passwordField.getText().trim());
        try {
            out.writeUTF(accessMessage);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String windowTitle) {
        Platform.runLater(()-> {
            if (windowTitle.equals("")) {
                stage.setTitle("ChatMe");
            } else {
                stage.setTitle("ChatMe [ " + windowTitle + " ]");
            }
        });
    }


}
