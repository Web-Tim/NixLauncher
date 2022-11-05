package eu.nix.nixlauncher.local.accounts;

import eu.nix.nixlauncher.Constants;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static final AccountManager instance = new AccountManager();

    private ArrayList<Account> accounts;

    public AccountManager() {
        this.accounts = new ArrayList<>();
        this.detectSaved();
    }

    public void addAccount(Account account) { this.accounts.add(account); }

    public Account getAccount(String username) {
        for (Account a : this.accounts) {
            if (a.getUsername().equals(username)) {
                return a;
            }
        }
        return null;
    }

    public void saveAccounts() {
        for (Account account : this.accounts) {
            try {
                if (!Constants.ACCOUNT_DIR.exists()) Constants.ACCOUNT_DIR.mkdirs();
                if (!Constants.ACCOUNT_FILE.exists()) Constants.ACCOUNT_FILE.createNewFile();

                Files.writeString(Paths.get(Constants.ACCOUNT_FILE.toURI()), String.format("%s:%s:%s:%s\n", account.getUsername(), account.getUuid(), account.getAccessToken(), account.getSkinURL()), StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public void detectSaved() {
        if (Constants.ACCOUNT_FILE.exists()) {
            try {
                List<String> fileContent = Files.readAllLines(Paths.get(Constants.ACCOUNT_FILE.toURI()));
                for (String line : fileContent) {
                    String[] split = line.split(":");
                    this.addAccount(new Account(split[0], split[1], split[2], split[3] + ":" + split[4]));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static AccountManager getInstance() {
        return instance;
    }

    public static class Account {
        private String username, uuid, accessToken;
        private String skinURL;

        public Account(String username, String uuid, String accessToken, String skinURL) {
            this.username = username;
            this.uuid = uuid;
            this.accessToken = accessToken;
            this.skinURL = skinURL;
        }

        public Account(MicrosoftAuthResult result) {
            this(result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), result.getProfile().getSkins()[0].getUrl());
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getSkinURL() {
            return skinURL;
        }

        public void setSkinURL(String skinURL) {
            this.skinURL = skinURL;
        }
    }
}
