package eu.nix.nixlauncher.util;

import javafx.concurrent.Task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadThread extends Task<Void> {
    private String fileURL;
    private File file;
    private long bytesDownloaded, totalSize, startTime;
    private double downloadSpeed;
    private boolean zipFile;

    public DownloadThread(File file, String fileURL, boolean zipFile)
    {
        this.file = file;
        this.fileURL = fileURL;
        this.zipFile = zipFile;
    }

    @Override
    protected Void call() throws Exception {
        this.startTime = System.currentTimeMillis();

        if (!this.file.getParentFile().exists()) this.file.getParentFile().mkdirs();
        if (!this.file.exists()) this.file.createNewFile();
        URLConnection connection = new URL(this.fileURL).openConnection();
        BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(this.file);

        byte dataBuffer[] = new byte[1024];
        this.totalSize = connection.getContentLength();
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);

            this.bytesDownloaded += bytesRead;
            long timeInSecs = (System.currentTimeMillis() - this.startTime) / 1000; //converting millis to seconds as 1000m in 1 second
            this.downloadSpeed = (this.bytesDownloaded / timeInSecs) / 1024D;
            updateProgress(this.getBytesDownloaded(), this.getTotalSize());
        }

        if (this.zipFile)
        {
            FileUtil.unzip(this.file);
        }
        return null;
    }

    public double getDownloadSpeed() {
        return downloadSpeed;
    }

    public long getBytesDownloaded() {
        return bytesDownloaded;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public File getFile() {
        return file;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }
}
