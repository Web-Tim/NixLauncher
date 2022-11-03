package eu.nix.nixlauncher;

import eu.nix.nixlauncher.util.WebUtil;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.print.attribute.standard.MediaSize;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainController {
    private static final MicrosoftAuthResult USER_INFO = LoginController.getResult();
    private static final MainController instance = new MainController();
    private static final File OUTPUT_DIR = new File("./bin"),
            NIX_FILE = new File(OUTPUT_DIR.getAbsolutePath() + "/NixClient.jar"),
            NATIVES_FILE = new File(OUTPUT_DIR.getAbsolutePath() + "/natives.zip"),
            NATIVES_DIR = new File(OUTPUT_DIR.getAbsolutePath() + "/natives"),
            ASSETS_FILE = new File(OUTPUT_DIR.getAbsolutePath() + "/assets.zip"),
            ASSETS_DIR = new File(OUTPUT_DIR.getAbsolutePath() + "/assets");
    private static final String VERSION_URL = "https://raw.githubusercontent.com/Web-Tim/NixConfigs/main/versions.txt",
            NATIVES_URL = "https://download1582.mediafire.com/wyi2dw86b4jg/i4fivr7nyprg46c/natives.zip",
            ASSETS_URL = "https://download1532.mediafire.com/ic6r4292i13g/hip6jeftjxbwqa3/assets.zip";

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

    private void launch()
    {
        if (!OUTPUT_DIR.exists()) OUTPUT_DIR.mkdir();

        HttpResponse<String> versionSite = WebUtil.get(VERSION_URL);
        String[] versions = versionSite.body().split(":");
        ArrayList<Float> versions_f = new ArrayList<>();
        for (String s : versions) {
            float version = Float.parseFloat(s.substring(1).contains("-") ? s.substring(1).split("-")[0] : s.substring(1));
            versions_f.add(version);
        }

        try {
            if (!NATIVES_DIR.exists())
            {
                InputStream in = new URL(NATIVES_URL).openStream();
                Files.copy(in, Paths.get(NATIVES_FILE.getAbsolutePath()));
                this.unzip(NATIVES_FILE.getAbsoluteFile());
            }

            if (!ASSETS_DIR.exists())
            {
                InputStream in = new URL(ASSETS_URL).openStream();
                Files.copy(in, Paths.get(ASSETS_FILE.getAbsolutePath()));
                this.unzip(ASSETS_FILE.getAbsoluteFile());
            }

            if (!NIX_FILE.exists()) {
                InputStream in = new URL(this.getDownloadURL(versions_f.get(0))).openStream();
                Files.copy(in, Paths.get(NIX_FILE.getAbsolutePath()));
            }
            this.progressBar.setProgress(0.5);
            this.progressLabel.setText("launching");

            ProcessBuilder builder = new ProcessBuilder("java", String.format("-Djava.library.path=%s", NATIVES_DIR.getAbsolutePath()), "-jar", "NixClient.jar")
                    .directory(new File("bin").getAbsoluteFile())
                    .redirectErrorStream(true);
            Process p = builder.start();
            System.out.println("Process: " + p.pid() + " started!");
            this.progressBar.setProgress(1);
            this.progressLabel.setText("launched!");

            this.mainPane.setDisable(true);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private void unzip(File zipFile) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            File newFile = newFile(zipFile.getParentFile(), entry);
            if (entry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            entry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        zipFile.delete();
    }

    private String getDownloadURL(float version) {
        return String.format("https://github.com/Web-Tim/NixClient/releases/download/v%s/Nix.jar", version);
    }

    public void prepare() {
        this.downloadHead();
    }

    private void downloadHead() {
        String url = USER_INFO.getProfile().getSkins()[0].getUrl();
        try {
            BufferedImage imgIn = ImageIO.read(new URL(url));
            ImageIO.write(resample(imgIn.getSubimage(8, 8, 8, 8), 8), "png", new File("src/main/resources/eu/nix/nixlauncher/img/skin/head.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param input
     * @param scaleFactor
     * @return
     * @see <a href="https://gist.github.com/jewelsea/5415891">ImageScaler.java</a>
     */
    private BufferedImage resample(BufferedImage input, int scaleFactor) {

        BufferedImage output = new BufferedImage(
                input.getWidth() * scaleFactor,
                input.getHeight() * scaleFactor,
                BufferedImage.TYPE_INT_RGB
        );

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                final int argb = input.getRGB(x, y);
                for (int dy = 0; dy < scaleFactor; dy++) {
                    for (int dx = 0; dx < scaleFactor; dx++) {
                        output.setRGB(x * scaleFactor + dx, y * scaleFactor + dy, argb);
                    }
                }
            }
        }

        return output;
    }

    public static MainController getInstance() {
        return instance;
    }
}
