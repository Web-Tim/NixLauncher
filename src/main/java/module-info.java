module eu.nix.nixlauncher {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.google.gson;
    requires openauth;
    requires java.desktop;
    requires java.net.http;

    opens eu.nix.nixlauncher to javafx.fxml;
    exports eu.nix.nixlauncher;
}