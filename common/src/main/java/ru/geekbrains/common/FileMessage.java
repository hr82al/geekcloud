package ru.geekbrains.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessage extends AbstractMessage {
    private String filename;
    private byte[] data;

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public FileMessage(Path path) throws IOException {
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }

    public FileMessage(String pathToFile) throws IOException {
        filename = Paths.get(pathToFile).getFileName().toString();
        data = Files.readAllBytes(Paths.get(pathToFile));
    }
}
