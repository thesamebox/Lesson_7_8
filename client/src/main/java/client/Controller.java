package client;

import commands.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.Buffer;
import java.util.*;


public class Controller implements Initializable {
    @FXML
    private ListView<String> clientList;
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
    private Stage regStage;
    private RegController regController;

    private static Socket socket;
    private static final int PORT = 8889;
    private static final String IP_ADDRESS = "localhost";

    private static DataInputStream in;
    private static DataOutputStream out;

    private boolean isAuth;

    private String nickname;

    private String fileHistoryName = makeHistoryFileName(nickname);


    private void setAuth(boolean auth) {
        this.isAuth = auth;
        messagePanel.setVisible(isAuth);
        messagePanel.setManaged(isAuth);
        authPanel.setVisible(!isAuth);
        authPanel.setManaged(!isAuth);
        clientList.setVisible(isAuth);
        clientList.setManaged(isAuth);
        if (!isAuth) {
            nickname = "";
        }
        setTitle(nickname);
        textArea.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(()-> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(windowEvent -> {
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF(Command.END);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
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
                                loadChatHistory();
                                break;
                            }
                            if (clientMessage.equals(Command.REG_OK)) {
                                regController.resultOfTryToReg(true);
                            }
                            if (clientMessage.equals(Command.REG_FAIl)) {
                                regController.resultOfTryToReg(false);

                            }
                        } else {
                            textArea.appendText(clientMessage + "\n");
                        }
                    }
                    // общение
                    while (true) {
                        String clientMessage = in.readUTF();
                        if (clientMessage.startsWith("/")) {
                            if (clientMessage.equals(Command.END)) {
                                System.out.println("Disconnected");
                                break;
                            }
                            if (clientMessage.startsWith(Command.CLIENT_LIST)) {
                                String[] token = clientMessage.split("\\s");
                                Platform.runLater(()-> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }
                        } else {
                            textArea.appendText(clientMessage + "\n");
                            saveChatHistory(clientMessage);
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
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

    @FXML
    public void clientListClick(MouseEvent mouseEvent) {
        System.out.println(clientList.getSelectionModel().getSelectedItem());
        String targetName = String.format("%s %s ", Command.WHISPER, clientList.getSelectionModel().getSelectedItem());
        textField.setText(targetName);
    }

    @FXML
    public void tryToRegistration(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();
            regController = fxmlLoader.getController();
            regController.setController(this);
            regStage = new Stage();
            regStage.setTitle("ChatMe registration");
            regStage.setScene(new Scene(root, 300, 400));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(String login, String password, String nickname) {
        String registrationData = String.format("%s %s %s %s", Command.REGISTRATION, login, password, nickname);
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(registrationData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeHistoryFileName(String nickname) {
        File historyFile = new File("..\\", nickname + ".txt");
        if (!historyFile.exists()) {
            return nickname + ".txt";
        }
        return null;
    }

    public void saveChatHistory(String message) throws IOException {
        PrintWriter outputStream = new PrintWriter(new FileWriter(makeHistoryFileName(nickname), true));
        outputStream.println(message);
    }

    public void loadChatHistory() throws IOException {
        File historyFile = new File("..\\", fileHistoryName);
        if (!historyFile.exists()) return;
        Scanner scanner = new Scanner(historyFile);
        int lines = 0;
        while (scanner.hasNextLine()) {
            lines++;
            scanner.nextLine();
        }
        scanner.close();

        int start = lines - 100;
        if(start < 0) start = 0;
        BufferedReader reader = new BufferedReader(new FileReader(historyFile));
        for (int i = start; i >= 0; i--) {
            textArea.appendText(reader.readLine());
        }
        reader.close();
    }
}
