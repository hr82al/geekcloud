package ru.geekbrains.common;

public class StatusMessage extends AbstractMessage {
    private String message;

    public StatusMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
