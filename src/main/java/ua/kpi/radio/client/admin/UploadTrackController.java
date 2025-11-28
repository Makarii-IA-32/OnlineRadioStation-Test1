package ua.kpi.radio.client.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import ua.kpi.radio.client.admin.facade.RadioAdminFacade;
import ua.kpi.radio.domain.Track;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class UploadTrackController {

    @FXML private StackPane dropArea;
    @FXML private TextField txtFilename;
    @FXML private TextField txtTitle;
    @FXML private TextField txtArtist;
    @FXML private TextField txtAlbum;
    @FXML private Label lblStatus;

    private File selectedFile;
    private final RadioAdminFacade facade = new RadioAdminFacade();

    @FXML
    public void initialize() {
        // Налаштування Drag-and-Drop
        dropArea.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropArea.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                // Перевіряємо чи це mp3
                if (file.getName().toLowerCase().endsWith(".mp3")) {
                    selectFile(file);
                    success = true;
                } else {
                    lblStatus.setText("Тільки MP3 файли!");
                    lblStatus.setStyle("-fx-text-fill: red;");
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void selectFile(File file) {
        this.selectedFile = file;
        txtFilename.setText(file.getName());
        lblStatus.setText("Файл обрано!");
        lblStatus.setStyle("-fx-text-fill: green;");

        // Автозаповнення Title з назви файлу (без розширення)
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            name = name.substring(0, dotIndex);
        }

        // ВИПРАВЛЕННЯ: Прибираємо перевірку if (txtTitle.getText().isEmpty())
        // Тепер поле оновлюється завжди, коли ми кидаємо новий файл
        txtTitle.setText(name);

        // Опціонально: можна очищати інші поля, щоб не плутати з даними попереднього файлу
        // txtArtist.clear();
        // txtAlbum.clear();
    }

    @FXML
    public void onSave() {
        if (selectedFile == null) {
            lblStatus.setText("Спочатку перетягніть файл!");
            lblStatus.setStyle("-fx-text-fill: red;");
            return;
        }
        if (txtTitle.getText().isBlank()) {
            lblStatus.setText("Введіть назву треку!");
            lblStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            // 1. Копіюємо файл у папку music-library
            Path destDir = Path.of("music-library");
            if (!Files.exists(destDir)) Files.createDirectories(destDir);

            Path destFile = destDir.resolve(selectedFile.getName());
            Files.copy(selectedFile.toPath(), destFile, StandardCopyOption.REPLACE_EXISTING);

            // 2. Створюємо об'єкт Track через BUILDER
            Track t = new Track.Builder()
                    .title(txtTitle.getText())
                    .artist(txtArtist.getText())
                    .album(txtAlbum.getText())
                    .audioPath("music-library/" + selectedFile.getName())
                    .coverPath("cover-library/default.jpg")
                    .build();

            // 3. Відправляємо дані на сервер
            facade.createTrack(t);

            lblStatus.setText("Успішно збережено!");
            lblStatus.setStyle("-fx-text-fill: green;");

            // Очищення після успішного збереження
            txtTitle.clear();
            txtArtist.clear();
            txtAlbum.clear();
            txtFilename.clear();
            selectedFile = null;

        } catch (Exception e) {
            // Виводимо помилку (наприклад, про дублікат) у лейбл
            lblStatus.setText("Помилка: " + e.getMessage());
            lblStatus.setStyle("-fx-text-fill: red;");
        }
    }
}