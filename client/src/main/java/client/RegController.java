package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {
    @FXML
    private TextField nicknameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField loginField;
    @FXML
    private TextArea textArea;
    @FXML
    private Button registration;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nickname = nicknameField.getText().trim();
        if(login.length()*password.length()*nickname.length() == 0) {
            textArea.appendText("Not enough data");
            return;
        }
        if(login.contains(" ") || password.contains(" ") || nickname.contains(" ")) {
            textArea.appendText("Only solid data allowed");
            return;
        }
        controller.tryToReg(login, password, nickname);
    }

    public void resultOfTryToReg(boolean success) {
        if (success) {
            textArea.appendText("Registration is successful.\n");
        } else {
            textArea.appendText("Registration is failed.\n Login or nickname have been taken probably.\n");
        }
    }
}
