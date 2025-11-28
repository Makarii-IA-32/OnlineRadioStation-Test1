package ua.kpi.radio.service.broadcasting;

import ua.kpi.radio.domain.RadioChannel;
import ua.kpi.radio.domain.Track;
import ua.kpi.radio.playlist.LoopingPlaylistIterator;
import ua.kpi.radio.service.RadioService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChannelBroadcaster implements Runnable {

    private final RadioChannel channelConfig;
    private final LoopingPlaylistIterator trackIterator;
    private final RadioService radioService = RadioService.getInstance();

    // Використовуємо інтерфейс замість конкретного Process
    private final StreamEncoder encoder;

    private volatile boolean running = true;

    private long currentTrackStartTime = 0;
    private long initialSeekMs = 0;
    private volatile long forcedSeekMs = -1;

    // Оновлений конструктор приймає енкодер
    public ChannelBroadcaster(RadioChannel channelConfig,
                              LoopingPlaylistIterator iterator,
                              long startOffsetMs,
                              StreamEncoder encoder) { // <--- Injection
        this.channelConfig = channelConfig;
        this.trackIterator = iterator;
        this.initialSeekMs = startOffsetMs;
        this.encoder = encoder;
    }

    public RadioChannel getChannelConfig() {
        return channelConfig;
    }

    @Override
    public void run() {
        Path outputDir = Paths.get("hls", channelConfig.getName());
        prepareDirectory(outputDir);

        System.out.println("Channel '" + channelConfig.getName() + "' started.");

        while (running) {
            Track track = trackIterator.next();
            if (track == null) {
                try { Thread.sleep(5000); } catch (InterruptedException e) { break; }
                continue;
            }

            radioService.updateNowPlaying(channelConfig.getId(), track);

            long seekToUse = 0;
            if (forcedSeekMs >= 0) {
                seekToUse = forcedSeekMs;
                forcedSeekMs = -1;
            } else {
                seekToUse = initialSeekMs;
                initialSeekMs = 0;
            }

            currentTrackStartTime = System.currentTimeMillis() - seekToUse;

            System.out.println("[" + channelConfig.getName() + "] Playing: " + track.getTitle()
                    + (seekToUse > 0 ? " (Resuming from " + (seekToUse/1000) + "s)" : ""));

            // ВИКЛИК АДАПТЕРА ЗАМІСТЬ ПРЯМОГО КОДУ FFmpeg
            encoder.stream(track, outputDir, seekToUse, channelConfig.getBitrate());
        }
    }

    public void restartWithNewBitrate(int newBitrate) {
        long currentPos = getCurrentTrackPositionMs();
        System.out.println("Restarting stream at " + currentPos + "ms with bitrate " + newBitrate);

        this.forcedSeekMs = currentPos;
        this.channelConfig.setBitrate(newBitrate);

        int currentIndex = trackIterator.getLastReturnedIndex();
        trackIterator.setIndex(currentIndex);

        // Зупиняємо через адаптер
        if (encoder.isAlive()) {
            encoder.stop();
        }
    }

    public void stop() {
        running = false;
        if (encoder.isAlive()) {
            encoder.stop();
        }
        radioService.clearNowPlaying(channelConfig.getId());
    }

    public int getCurrentTrackIndex() {
        return trackIterator.getLastReturnedIndex();
    }

    public long getCurrentTrackPositionMs() {
        if (currentTrackStartTime == 0) return 0;
        long pos = System.currentTimeMillis() - currentTrackStartTime;
        return Math.max(0, pos);
    }

    public void jumpToTrack(int index) {
        trackIterator.setIndex(index);
        if (encoder.isAlive()) {
            encoder.stop();
        }
    }

    public void skipTrack() {
        if (encoder.isAlive()) {
            encoder.stop();
        }
    }

    // Приватний метод prepareDirectory залишається без змін...
    private void prepareDirectory(Path dir) {
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
            File[] files = dir.toFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".ts") || f.getName().endsWith(".m3u8")) {
                        f.delete();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}