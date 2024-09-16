import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;

public class DriveApplication extends Application {

    private static TableView<DriveFile> table;

    private static ObservableList<DriveFile> fileList;

    private static void refreshTable() throws IOException {
        fileList = FXCollections.observableArrayList(DriveMain.getFiles(true));
        table.setItems(fileList);
        table.scrollTo(0);
    }

    private static void loadMoreItems() throws IOException {
        fileList.addAll(DriveMain.getFiles(false));
    }

    private static void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to check if the user is at the bottom of the table
    private boolean isScrolledToBottom(TableView<?> table) {
        TableViewSkin<?> skin = (TableViewSkin<?>)table.getSkin();
        if (skin != null) {
            VirtualFlow<?> virtualFlow = (VirtualFlow<?>) skin.getChildren().get(1);
            int lastVisibleIndex = virtualFlow.getLastVisibleCell().getIndex();
            int lastIndex = table.getItems().size()-1;
            return lastVisibleIndex == lastIndex;
        }
        return false;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        table = new TableView<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

        TableColumn<DriveFile, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().Name));

        TableColumn<DriveFile, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().Type));

        TableColumn<DriveFile, String> dateColumn = new TableColumn<>("Modified Date");
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().LastModifiedDate.toString()));
//        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));

        table.getColumns().addAll(nameColumn, typeColumn, dateColumn);

        refreshTable();

        Button uploadButton = new Button("Upload");
        Button downloadButton = new Button("Download");
        Button refreshButton = new Button("Refresh");
        Button deleteButton = new Button("Delete");

        // Upload button action
        uploadButton.setOnAction(event -> {
            System.out.println("Upload button clicked.");
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                String selectedFilePath = selectedFile.getAbsolutePath();
                System.out.println("Selected file: " + selectedFilePath);
                try {
                    DriveMain.uploadFile(selectedFilePath);
                    refreshTable();
                    alert("Upload Successful!");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("No file selected.");
            }
        });

        // Download button action
        downloadButton.setOnAction(event -> {
            DriveFile selectedItem = table.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                DirectoryChooser directoryChooser = new DirectoryChooser();

                File selectedDirectory = directoryChooser.showDialog(primaryStage);

                if (selectedDirectory != null) {
                    String directoryPath = selectedDirectory.getAbsolutePath();
                    System.out.println("Selected directory: " + directoryPath);

                    System.out.println("Downloading: " + selectedItem);
                    try {
                        DriveMain.downloadFile(selectedItem, directoryPath);
                        alert("Download Succeeded!");
                    } catch (Exception e) {
                        error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("No directory selected");
                }
            } else {
                System.out.println("No file selected for download.");
            }
        });

        // Refresh button action
        refreshButton.setOnAction(event -> {
            try {
                refreshTable();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Delete button action
        deleteButton.setOnAction(event -> {
            DriveFile selectedItem = table.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                System.out.println("Deleting: " + selectedItem.getName());
                try {
                    DriveMain.deleteFile(selectedItem);
                    refreshTable();
                    alert("Delete Successful!");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("No file selected for deletion.");
            }
        });

        table.setOnScroll(event -> {
            if (isScrolledToBottom(table)) {
                System.out.println("SCROLLED TO BOTTOM");
                try {
                    loadMoreItems();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        HBox buttonLayout = new HBox(40);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().addAll(uploadButton, downloadButton, refreshButton, deleteButton);
        VBox.setMargin(buttonLayout, new Insets(0,0,10,0));

        VBox layout = new VBox(10);
        layout.getChildren().addAll(table, buttonLayout);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setTitle("Google Docs Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        DriveMain.initService();
        launch(args);
    }
}
