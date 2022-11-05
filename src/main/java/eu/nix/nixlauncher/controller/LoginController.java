package eu.nix.nixlauncher.controller;

import eu.nix.nixlauncher.Application;
import eu.nix.nixlauncher.local.accounts.AccountManager;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button submitBtn;

    private boolean loginSuccess;
    private static MicrosoftAuthResult result;

    public void loginSubmit() {
        if (this.emailField.getText().isEmpty() || this.passwordField.getText().isEmpty()) {
            this.loginSuccess = false;
        } else {
            try {
                MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                result = authenticator.loginWithCredentials(this.emailField.getText(), this.passwordField.getText());
                this.loginSuccess = result.getProfile() != null;
            } catch (MicrosoftAuthenticationException e) {
                throw new RuntimeException(e);
            }
        }

        if (this.loginSuccess) {
            AccountManager.Account account = new AccountManager.Account(LoginController.result);
            AccountManager.getInstance().addAccount(account);
            AccountManager.getInstance().saveAccounts();
            MainController.USER = account;
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main.fxml"));
                Scene mainScene = new Scene(fxmlLoader.load(), 640, 360);

                Application.getPrimaryStage().setScene(mainScene);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            DropShadow effect = new DropShadow();
            effect.setBlurType(BlurType.GAUSSIAN);
            effect.setWidth(22);
            effect.setHeight(22);
            effect.setRadius(10);
            effect.setColor(new Color(0.6711, 0.1305, 0.1305, 1.0));
            this.submitBtn.setEffect(effect);
        }
    }

    public void emailTyped() {
        DropShadow effect = new DropShadow();
        effect.setBlurType(BlurType.GAUSSIAN);
        effect.setWidth(21);
        effect.setHeight(21);
        effect.setRadius(10);
        if (this.emailField.getText().contains("@")) {
            effect.setColor(new Color(0.1782, 0.2895, 0.219, 1.0));
        } else {
            effect.setColor(new Color(0.6711, 0.1305, 0.1305, 1.0));
        }
        this.emailField.setEffect(effect);

        if (this.emailField.getText().isEmpty() && this.emailField.getEffect().equals(effect))
            this.emailField.setEffect(null);
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public static MicrosoftAuthResult getResult() {
        return result;
    }

    public static void setResult(MicrosoftAuthResult result) {
        LoginController.result = result;
    }
}