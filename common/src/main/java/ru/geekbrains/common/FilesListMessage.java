package ru.geekbrains.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class FilesListMessage extends AbstractMessage {
    private List<String> files;

    public FilesListMessage() throws IOException{
        Path path = Paths.get("server_storage");
        files = new LinkedList<>();
        Files.newDirectoryStream(path).forEach((s)-> {
            files.add(s.getFileName().toString());
        });
    }

    public List<String> getFiles() {
        return files;
    }
}
