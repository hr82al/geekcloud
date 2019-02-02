package ru.geekbrains.common;

public class RenameFileRequest {
    private String newFilename;

    public RenameFileRequest(String filename) {
        newFilename = filename;
    }

    public String getNewFilename() {
        return newFilename;
    }
}
