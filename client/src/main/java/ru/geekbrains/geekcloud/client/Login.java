package ru.geekbrains.geekcloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import ru.geekbrains.common.AuthRequest;

import java.net.URL;
import java.util.ResourceBundle;

public class Login implements Initializable {
    @FXML
    TextField loginTF;
    @FXML
    PasswordField passwordPF;
    @FXML
    HBox globParent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginTF.requestFocus();
    }

    public void pressOnLoginTF(ActionEvent actionEvent) {
        passwordPF.requestFocus();
    }

    public void pressOnPasswordPF(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            Network.sendMsg(new AuthRequest(loginTF.getText(), passwordPF.getText()));
            globParent.getScene().getWindow().hide();
        });
    }
}
