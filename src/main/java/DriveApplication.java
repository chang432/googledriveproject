import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DriveApplication extends Application {

    private static TableView<DriveFile> table;

    private static void refreshTable() throws IOException {
        ObservableList<DriveFile> fileList = FXCollections.observableArrayList(DriveMain.getFiles());
        table.setItems(fileList);
    }

    private static void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        ObservableList<DriveFile> fileList = FXCollections.observableArrayList(DriveMain.getFiles());

        table = new TableView<>();

        TableColumn<DriveFile, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().Name));

        TableColumn<DriveFile, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().Type));

        TableColumn<DriveFile, String> dateColumn = new TableColumn<>("Modified Date");
//        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().LastModifiedDate.toString()));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));

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
                    } catch (IOException e) {
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

        VBox layout = new VBox(10);
        layout.getChildren().addAll(table, uploadButton, downloadButton, refreshButton, deleteButton);

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
