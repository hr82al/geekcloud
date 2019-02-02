package ru.geekbrains.geekcloud.client;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
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
    private static final String SEND_FILE = "Отравить файл на сервер";

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
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    else if (am instanceof FilesListMessage) {
                        Platform.runLater(() -> {
                            serverFilesList.getItems().clear();
                            serverFilesList.getItems().addAll(((FilesListMessage) am).getFiles());
                        });
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
        //clientFilesList.setItems(FXCollections.observableArrayList());
        refreshLocalFilesList();
        refreshServerFilesList();
        initializeDragAndDrop(tfFileName);
        initializeDragAndDrop(serverFilesList);
        setContextMenu(serverFilesList);
    }

    private void setContextMenu(ListView<String> item) {
        final ContextMenu contextMenu = new ContextMenu();
/*        contextMenu.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    System.out.println("right clicked");
                    event.consume();
                }
            }
        });
        contextMenu.setOnAction((e) -> {
            System.out.println("cl");
        });*/
        MenuItem menuItem1 = new MenuItem(SEND_FILE);
        menuItem1.setOnAction(e -> {
            System.out.println("send");
            System.out.println(e.getSource().toString());
        });
        contextMenu.getItems().addAll(menuItem1);
        item.setContextMenu(contextMenu);
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

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(tfFileName.getText()));
            tfFileName.clear();
        }
    }

    public void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                clientFilesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    clientFilesList.getItems().clear();
                    Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        String file = tfFileName.getText().trim();
        if(Files.exists(Paths.get(file))) {
            try {
                FileMessage fm = new FileMessage(file);
                Network.sendMsg(fm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tfFileName.clear();
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
