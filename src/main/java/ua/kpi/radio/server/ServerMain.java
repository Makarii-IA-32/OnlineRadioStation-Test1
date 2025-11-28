package ua.kpi.radio.server;

import ua.kpi.radio.repo.Database;
import ua.kpi.radio.repo.SQLiteStatsRepository;
import ua.kpi.radio.repo.StatsRepository;
import ua.kpi.radio.repo.RadioChannelRepository;
import ua.kpi.radio.repo.SQLiteRadioChannelRepository;
import ua.kpi.radio.server.http.HttpServerLauncher;
import ua.kpi.radio.service.DemoDataInitializer;
import ua.kpi.radio.service.RadioService;
import ua.kpi.radio.domain.RadioChannel;

import java.util.Timer;
import java.util.TimerTask;

public class ServerMain {

    public static void main(String[] args) {
        try {
            System.out.println("Initializing database...");
            Database.init();

            System.out.println("Seeding demo data...");
            new DemoDataInitializer().initDemoData();

            System.out.println("Starting HTTP server...");
            new HttpServerLauncher().start(8080);

            // --- ЗАПУСК ЗБОРУ СТАТИСТИКИ ---
            startStatisticsCollector();

            System.out.println("Server started. Press Ctrl+C to stop.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startStatisticsCollector() {
        StatsRepository statsRepo = new SQLiteStatsRepository();
        RadioChannelRepository channelRepo = new SQLiteRadioChannelRepository();
        RadioService service = RadioService.getInstance();

        Timer timer = new Timer(true); // Daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // Проходимо по всіх каналах
                    for (RadioChannel ch : channelRepo.findAll()) {
                        int count = service.getActiveListenersCount(ch.getId());
                        // Записуємо в БД, навіть якщо 0 (щоб бачити історію)
                        statsRepo.saveStat(ch.getId(), count);
                    }
                    // System.out.println("Statistics saved.");
                } catch (Exception e) {
                    System.err.println("Failed to save stats: " + e.getMessage());
                }
            }
        }, 60000, 60000); // Затримка 1 хв, повтор кожну 1 хв
    }
}