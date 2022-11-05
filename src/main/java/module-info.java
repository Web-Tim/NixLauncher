module eu.nix.nixlauncher {
    requires javafx.controls;
    requires javafx.fxml;

    requires openauth;
    requires java.desktop;
    requires java.net.http;
    requires java.sql;

    opens eu.nix.nixlauncher to javafx.fxml;
    exports eu.nix.nixlauncher;
    exports eu.nix.nixlauncher.controller;
    exports eu.nix.nixlauncher.local.accounts;
    opens eu.nix.nixlauncher.controller to javafx.fxml;
}