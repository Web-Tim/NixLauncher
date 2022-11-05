package eu.nix.nixlauncher.controller;

import eu.nix.nixlauncher.Application;
import eu.nix.nixlauncher.Constants;
import eu.nix.nixlauncher.local.accounts.AccountManager;
import eu.nix.nixlauncher.util.DownloadThread;
import eu.nix.nixlauncher.util.WebUtil;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class MainController {
    private static final MainController instance = new MainController();

    private float latestVersion;
    public static AccountManager.Account USER;

    @FXML
    private AnchorPane mainPane;
    @FXML
    private Text pInformationLabel;
    @FXML
    private ImageView launchPic;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Text progressLabel;
    @FXML
    private VBox configBox;
    @FXML
    private ImageView skinImg;

    @FXML
    public void initialize() {
        if (USER == null) System.out.println("wdwa");
        Platform.runLater(() -> {
            assert USER != null;
            this.skinImg.setImage(new Image(Constants.getSkinFile(USER).getAbsolutePath()));
        });
    }

    public void profilePicEntered() {
        FadeTransition fade = new FadeTransition();
        fade.setDuration(Duration.millis(230));
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setCycleCount(1);
        fade.setAutoReverse(false);
        fade.setNode(this.pInformationLabel);
        fade.play();
    }

    public void profilePicExited() {
        this.pInformationLabel.setOpacity(0);
    }

    public void launchPicClicked() {
        this.progressLabel.setText("");
        this.progressLabel.setDisable(true);
        this.progressBar.setDisable(true);
        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(-45);
        rotateTransition.setDuration(Duration.millis(400));
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(false);
        rotateTransition.setNode(launchPic);

        TranslateTransition translateTransition = new TranslateTransition();
        translateTransition.setFromY(88);
        translateTransition.setToY(-300);
        translateTransition.setDuration(Duration.millis(800));
        translateTransition.setCycleCount(1);
        translateTransition.setAutoReverse(false);
        translateTransition.setNode(launchPic);

        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setDuration(Duration.millis(1200));
        fadeTransition.setCycleCount(1);
        fadeTransition.setNode(progressBar);

        FadeTransition fadeTransition2 = new FadeTransition();
        fadeTransition2.setFromValue(0);
        fadeTransition2.setToValue(1);
        fadeTransition2.setDuration(Duration.millis(1200));
        fadeTransition2.setCycleCount(1);
        fadeTransition2.setNode(progressLabel);

        rotateTransition.play();
        rotateTransition.setOnFinished(finish -> translateTransition.play());
        translateTransition.setOnFinished(finish -> fadeTransition.play());
        fadeTransition.setOnFinished(finish -> fadeTransition2.play());
        fadeTransition2.setOnFinished(finish -> this.launch());
    }

    private void launch() {
        this.progressLabel.setDisable(false);
        this.progressBar.setDisable(false);

        Service<Void> version = Constants.bindTask(this.progressBar, new Task<Void>() {
            @Override
            protected Void call() {
                progressLabel.setText("getting version");
                latestVersion = fetchLatestVersion();
                return null;
            }
        });
        if (!Constants.OUTPUT_DIR.exists()) Constants.OUTPUT_DIR.mkdirs();
        version.setOnSucceeded(workerStateEvent -> this.downloadNatives());
        version.start();
    }

    private void downloadNatives() {
        if (!Constants.NATIVES_DIR.exists()) {
            progressLabel.setText("downloading natives");
            Service<Void> natives = Constants.bindTask(this.progressBar, new DownloadThread(Constants.NATIVES_FILE, Constants.NATIVES_URL, true));
            natives.setOnSucceeded(workerStateEvent -> this.downloadAssets());
            natives.start();
        }else {
            this.downloadAssets();
        }
    }

    private void downloadAssets() {
        if (!Constants.ASSETS_DIR.exists()) {
            progressLabel.setText("downloading assets");
            Service<Void> assets = Constants.bindTask(this.progressBar, new DownloadThread(Constants.ASSETS_FILE, Constants.ASSETS_URL, true));
            assets.setOnSucceeded(workerStateEvent -> this.downloadJar(this.latestVersion));
            assets.start();
        }else {
            this.downloadJar(this.latestVersion);
        }
    }

    private void downloadJar(float version) {
        if (!Constants.NIX_FILE.exists()) {
            progressLabel.setText("downloading jar");
            Service<Void> jar = Constants.bindTask(this.progressBar, new DownloadThread(Constants.NIX_FILE, this.getDownloadURL(version), false));
            jar.setOnSucceeded(workerStateEvent -> this.runClient());
            jar.start();
        }else {
            this.runClient();
        }
    }

    private void runClient() {
        Service<Void> run = Constants.bindTask(this.progressBar, new Task<>() {
            @Override
            protected Void call() throws Exception {
                progressLabel.setText("running jar");
                ProcessBuilder builder = new ProcessBuilder("java", String.format("-Djava.library.path=%s", Constants.NATIVES_DIR.getAbsolutePath()), "-jar", "NixClient.jar")
                        .directory(new File("bin/versions/" + latestVersion).getAbsoluteFile())
                        .redirectErrorStream(true);
                Process p = builder.start();
                System.out.println("Process: " + p.pid() + " started!");

                Application.getPrimaryStage().hide();
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                }
                input.close();
                return null;
            }
        });
        run.start();
    }

    private float fetchLatestVersion() {
        HttpResponse<String> versionSite = WebUtil.get(Constants.VERSION_URL);
        String[] versions = versionSite.body().split(":");
        ArrayList<Float> versions_f = new ArrayList<>();
        for (String s : versions) {
            float version = Float.parseFloat(s.substring(1).contains("-") ? s.substring(1).split("-")[0] : s.substring(1));
            versions_f.add(version);
        }
        this.reassign(versions_f.get(0));
        return versions_f.get(0);
    }

    private void reassign(float version) {
        Constants.OUTPUT_DIR = new File(Constants.OUTPUT_DIR + "/versions/" + version);
        Constants.NIX_FILE = new File(Constants.OUTPUT_DIR.getAbsolutePath() + "/NixClient.jar");
        Constants.NATIVES_FILE = new File(Constants.OUTPUT_DIR.getAbsolutePath() + "/natives.zip");
        Constants.NATIVES_DIR = new File(Constants.OUTPUT_DIR.getAbsolutePath() + "/natives");
        Constants.ASSETS_FILE = new File(Constants.OUTPUT_DIR.getAbsolutePath() + "/assets.zip");
        Constants.ASSETS_DIR = new File(Constants.OUTPUT_DIR.getAbsolutePath() + "/assets");
    }

    private String getDownloadURL(float version) {
        return String.format("https://github.com/Web-Tim/NixClient/releases/download/v%s/Nix.jar", version);
    }

    public static MainController getInstance() {
        return instance;
    }
}
