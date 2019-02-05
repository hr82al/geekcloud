package ru.geekbrains.common;

public class DeleteFileRequest extends AbstractMessage {
    private String filename;

    public DeleteFileRequest(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
