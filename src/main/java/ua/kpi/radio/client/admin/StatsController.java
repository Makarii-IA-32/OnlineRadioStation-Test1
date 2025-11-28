package ua.kpi.radio.client.admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import ua.kpi.radio.client.admin.facade.RadioAdminFacade;
import ua.kpi.radio.domain.RadioChannel;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatsController {

    @FXML private ComboBox<RadioChannel> comboChannels;
    @FXML private LineChart<String, Number> lineChart;

    private final RadioAdminFacade facade = new RadioAdminFacade();

    @FXML
    public void initialize() {
        // Налаштування ComboBox для правильного відображення назв каналів
        comboChannels.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(RadioChannel object) {
                return object == null ? "" : object.getName();
            }
            @Override
            public RadioChannel fromString(String string) { return null; }
        });

        loadChannels();
    }

    private void loadChannels() {
        new Thread(() -> {
            try {
                List<RadioChannel> channels = facade.getAllChannels();
                Platform.runLater(() -> {
                    comboChannels.getItems().setAll(channels);
                    if (!channels.isEmpty()) {
                        comboChannels.getSelectionModel().select(0);
                        onChannelSelected();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void onChannelSelected() {
        refreshStats();
    }

    @FXML
    public void refreshStats() {
        RadioChannel selected = comboChannels.getValue();
        if (selected == null) return;

        new Thread(() -> {
            try {
                Map<String, Double> data = facade.getChannelStats(selected.getId());

                // Сортуємо дані за часом (бо LinkedHashMap може збитися при передачі JSON)
                // TreeMap автоматично сортує ключі (String time)
                Map<String, Double> sortedData = new TreeMap<>(data);

                Platform.runLater(() -> updateChart(selected.getName(), sortedData));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Помилка завантаження: " + e.getMessage()).show());
            }
        }).start();
    }

    private void updateChart(String channelName, Map<String, Double> data) {
        lineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(channelName);

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        lineChart.getData().add(series);
    }
}