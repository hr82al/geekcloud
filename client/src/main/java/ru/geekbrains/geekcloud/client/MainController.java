package ru.geekbrains.geekcloud.client;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.geekbrains.common.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final String CLIENT_STORAGE = "client_storage";
    private static final String UPLOAD_FILE = "Отравить файл на сервер";
    private static final String DELETE_FILE = "Удалить файл";
    private static final String DOWNLOAD_FILE = "Скачать файл";
    private static final String RENAME_FILE = "Переименовать файл";

    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> clientFilesList;

    @FXML
    ListView<String> serverFilesList;

    private List<String> sfl;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get(CLIENT_STORAGE + "/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    else if (am instanceof FilesListMessage) {
                        Platform.runLater(() -> {
                            serverFilesList.getItems().clear();
                            serverFilesList.getItems().addAll(((FilesListMessage) am).getFiles());
                        });
                    }
                    else if (am instanceof DeleteFileMessage) {
                        refreshServerFilesList();
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
        refreshServerFilesList();
        initializeDragAndDrop(tfFileName);
        initializeDragAndDrop(serverFilesList);
        setServerContextMenu();
        setClientContextMenu();
    }

    private void setClientContextMenu() {
        clientFilesList.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        tfFileName.setText(newValue);
                    }
                });

        final ContextMenu contextMenu = new ContextMenu();

        MenuItem menuItem1 = new MenuItem(UPLOAD_FILE);
        menuItem1.setOnAction(e -> {
            uploadFile(tfFileName.getText().trim());
            tfFileName.clear();
        });

        contextMenu.getItems().addAll(menuItem1);
        clientFilesList.setContextMenu(contextMenu);
    }

    private void setServerContextMenu() {
        serverFilesList.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        tfFileName.setText(newValue);
                    }
                });

        final ContextMenu contextMenu = new ContextMenu();

        MenuItem menuItem1 = new MenuItem(DOWNLOAD_FILE);
        menuItem1.setOnAction(e -> {
            downloadFile(tfFileName.getText());
        });

        MenuItem menuItem2 = new MenuItem(DELETE_FILE);
        menuItem2.setOnAction(e -> {
            deleteFile(serverFilesList.getSelectionModel().getSelectedItem());
        });

        MenuItem menuItem3 = new MenuItem(RENAME_FILE);
        menuItem3.setOnAction(e -> {
            try {
                Stage stage = new Stage();
                //FXMLLoader loader = new FXMLLoader(getClass().getResource("/Rename.fxml"));
                Parent root = FXMLLoader.load(getClass().getResource("/Rename "));
                //RenameController renameController = (RenameController) loader.getController();
                stage.setTitle("Переименование");
                stage.setScene(new Scene(root, 300, 100));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        contextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);
        serverFilesList.setContextMenu(contextMenu);
    }

    private void refreshServerFilesList() {
            if (Platform.isFxApplicationThread()) {
                Network.sendMsg(new FilesListRequest());
            } else {
                Platform.runLater(() -> {
                    Network.sendMsg(new FilesListRequest());;
                });
            }
    }

    private void deleteFile(String filename) {
        if (Platform.isFxApplicationThread()) {
            Network.sendMsg(new DeleteFileRequest(filename));
        } else {
            Platform.runLater(() -> {
                Network.sendMsg(new DeleteFileRequest(filename));
            });
        }
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            downloadFile(tfFileName.getText());
        }
    }

    private void downloadFile(String file) {
        if (Platform.isFxApplicationThread()) {
            Network.sendMsg(new FileRequest(file));
            tfFileName.clear();
        } else {
            Platform.runLater(() -> {
                Network.sendMsg(new FileRequest(file));
                tfFileName.clear();
            });
        }
    }


    public void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                clientFilesList.getItems().clear();
                Files.list(Paths.get(CLIENT_STORAGE)).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    clientFilesList.getItems().clear();
                    Files.list(Paths.get(CLIENT_STORAGE)).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        uploadFile(tfFileName.getText().trim());
        tfFileName.clear();
    }

    private void uploadFile(String file) {
        Platform.runLater(() -> {
            if(Files.exists(Paths.get(file))) {
                try {
                    FileMessage fm = new FileMessage(file);
                    Network.sendMsg(fm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (Files.exists(Paths.get(CLIENT_STORAGE + "/" + file))) {
                try {
                    FileMessage fm = new FileMessage(Paths.get(CLIENT_STORAGE + "/" + file));
                    Network.sendMsg(fm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void initializeDragAndDrop(Control item) {
        item.setOnDragOver(event -> {
            if (event.getGestureSource() != item && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        item.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                tfFileName.setText("");
                for (File o : db.getFiles()) {
                    tfFileName.setText(tfFileName.getText() + o.getAbsolutePath() + " ");
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
