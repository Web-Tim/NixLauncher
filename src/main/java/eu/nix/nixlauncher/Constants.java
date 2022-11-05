package eu.nix.nixlauncher;

import eu.nix.nixlauncher.local.accounts.AccountManager;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

import java.io.File;

public class Constants {
    public static File OUTPUT_DIR = new File("./bin");
    public static File NIX_FILE = new File(OUTPUT_DIR.getAbsolutePath() + "/NixClient.jar"),
            NATIVES_FILE = new File(OUTPUT_DIR.getAbsolutePath() + "/natives.zip"),
            NATIVES_DIR = new File(OUTPUT_DIR.getAbsolutePath() + "/natives"),
            ASSETS_FILE = new File(OUTPUT_DIR.getAbsolutePath() + "/assets.zip"),
            ASSETS_DIR = new File(OUTPUT_DIR.getAbsolutePath() + "/assets");

    public static final String VERSION_URL = "https://raw.githubusercontent.com/Web-Tim/NixConfigs/main/versions.txt",
            NATIVES_URL = "https://onedrive.live.com/download?cid=7501314E20A63421&resid=7501314E20A63421%217509&authkey=AMUmWRJMPBg6z7M",
            ASSETS_URL = "https://onedrive.live.com/download?cid=7501314E20A63421&resid=7501314E20A63421%217508&authkey=ALtVKBzSz5rJ1Hw";

    public static final File ACCOUNT_DIR = new File("./bin/accounts"),
            ACCOUNT_FILE = new File(ACCOUNT_DIR + "/accounts.cfg"),
            SKIN_DIR = new File(ACCOUNT_DIR + "/skin");

    public static File getSkinFile(AccountManager.Account account) {
        return new File(SKIN_DIR + "/" + account.getUsername() + ".png");
    }

    public static Service<Void> bindTask(ProgressBar progressBar, Task<Void> task) {
        Service<Void> s = Constants.createService(task);
        progressBar.progressProperty().bind(s.progressProperty());
        return s;
    }

    public static Service<Void> createService(Task<Void> task) {
        return new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return task;
            }
        };
    }
}
