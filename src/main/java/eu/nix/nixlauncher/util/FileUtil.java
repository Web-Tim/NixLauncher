package eu.nix.nixlauncher.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    /**
     * @see <a href="https://gist.github.com/jewelsea/5415891">ImageScaler.java</a>
     */
    public static BufferedImage resample(BufferedImage input, int scaleFactor) {

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

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static void unzip(File zipFile) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            File newFile = newFile(zipFile, entry);
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
}
