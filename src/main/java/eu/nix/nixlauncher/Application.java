package eu.nix.nixlauncher;

import eu.nix.nixlauncher.local.accounts.AccountManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private static Stage primaryStage;
    private static Scene loginScene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader;
        if (!AccountManager.getInstance().getAccounts().isEmpty())
        {
            fxmlLoader = new FXMLLoader(Application.class.getResource("choose-login.fxml"));
        }else {
            fxmlLoader = new FXMLLoader(Application.class.getResource("login.fxml"));
        }
        Application.loginScene = new Scene(fxmlLoader.load(), 640, 360);
        stage.setResizable(false);
        stage.setTitle("NixLauncher");
        stage.setScene(Application.loginScene);
        stage.show();
        Application.primaryStage = stage;
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Scene getLoginScene() {
        return loginScene;
    }
}