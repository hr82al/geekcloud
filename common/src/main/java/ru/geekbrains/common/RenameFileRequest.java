package ru.geekbrains.common;

public class RenameFileRequest extends AbstractMessage {
    private String oldFileName;
    private String newFileName;

    public RenameFileRequest(String oldFileName, String newFileName) {
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    public String getOldFileName() {
        return oldFileName;
    }

    public String getNewFileName() {
        return newFileName;
    }
}
