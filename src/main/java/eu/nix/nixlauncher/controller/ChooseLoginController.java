package eu.nix.nixlauncher.controller;

import eu.nix.nixlauncher.Application;
import eu.nix.nixlauncher.Constants;
import eu.nix.nixlauncher.local.accounts.AccountManager;
import eu.nix.nixlauncher.util.FileUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ChooseLoginController {
    @FXML
    private VBox accountsBox;
    @FXML
    private AnchorPane mainPane;

    @FXML
    public void initialize() {
        for (AccountManager.Account account : AccountManager.getInstance().getAccounts()) {
            this.handleSkinFile(account);
        }
    }

    private void handleSkinFile(AccountManager.Account account) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!Constants.getSkinFile(account).getParentFile().exists()) Constants.getSkinFile(account).getParentFile().mkdirs();
                    if (!Constants.getSkinFile(account).exists()) Constants.getSkinFile(account).createNewFile();

                    BufferedImage imgIn = ImageIO.read(new URL(account.getSkinURL()));
                    ImageIO.write(FileUtil.resample(imgIn.getSubimage(8, 8, 8, 8), 6), "png", Constants.getSkinFile(account));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.handleView(account);
    }

    private void handleView(AccountManager.Account account) {
        HBox elem = new HBox();
        elem.setSpacing(10);
        elem.setCursor(Cursor.HAND);
        elem.setPadding(new Insets(0, 0, 0, 45));

        ImageView skin = new ImageView();
        skin.setFitWidth(32);
        skin.setFitHeight(32);
        skin.setImage(new Image(Constants.getSkinFile(account).getAbsolutePath()));
        skin.setSmooth(true);

        DropShadow shadow = new DropShadow(BlurType.GAUSSIAN, Color.color(0, 0, 0), 29.5f, 0.0f, 0.0f, 0.0f);
        shadow.setWidth(60);
        shadow.setHeight(60);
        skin.setEffect(shadow);

        Text acc = new Text(account.getUsername());
        acc.setFontSmoothingType(FontSmoothingType.LCD);
        acc.setFill(Color.color(245 / 255f, 245 / 255f, 245 / 255f));
        acc.setStyle("-fx-font: 22 system;");

        elem.getChildren().add(skin);
        elem.getChildren().add(acc);
        elem.setOnMouseClicked(event -> this.clickElement(event, account));
        this.accountsBox.getChildren().add(elem);
    }

    private void clickElement(MouseEvent event, AccountManager.Account account) {
        MainController.USER = account;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main.fxml"));
            Scene mainScene = new Scene(fxmlLoader.load(), 640, 360);

            Application.getPrimaryStage().setScene(mainScene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
