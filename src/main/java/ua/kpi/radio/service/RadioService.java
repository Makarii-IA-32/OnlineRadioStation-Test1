package ua.kpi.radio.service;

import ua.kpi.radio.domain.Track;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RadioService {
    private static final RadioService INSTANCE = new RadioService();
    private final Map<Integer, Track> channelTracks = new ConcurrentHashMap<>();

    // Мапа для підрахунку: ChannelID -> (UserID -> Останній час активності)
    private final Map<Integer, Map<String, Long>> listenersHeartbeats = new ConcurrentHashMap<>();

    private RadioService() {}

    public static RadioService getInstance() { return INSTANCE; }

    public void updateNowPlaying(int channelId, Track track) {
        channelTracks.put(channelId, track);
    }

    public void clearNowPlaying(int channelId) {
        channelTracks.remove(channelId);
        listenersHeartbeats.remove(channelId);
    }

    // Метод, який викликає NowPlayingHandler
    public void registerHeartbeat(int channelId, String userId) {
        listenersHeartbeats.computeIfAbsent(channelId, k -> new ConcurrentHashMap<>())
                .put(userId, System.currentTimeMillis());
    }

    // Метод для отримання кількості слухачів (очищає старих)
    public int getActiveListenersCount(int channelId) {
        Map<String, Long> channelListeners = listenersHeartbeats.get(channelId);
        if (channelListeners == null) return 0;

        long now = System.currentTimeMillis();
        long threshold = 5 * 1000; // Вважаємо активними тих, хто був тут останні 15 сек

        Iterator<Map.Entry<String, Long>> it = channelListeners.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue() > threshold) {
                it.remove();
            }
        }
        return channelListeners.size();
    }

    public NowPlayingInfo getNowPlayingInfo(int channelId) {
        NowPlayingInfo info = new NowPlayingInfo();
        Track currentTrack = channelTracks.get(channelId);

        info.setListeners(getActiveListenersCount(channelId)); // <--- Передаємо цифру в UI

        if (currentTrack == null) {
            info.setTitle("Очікування треку...");
            info.setArtist("");
            return info;
        }
        info.setTrackId(currentTrack.getId());
        info.setTitle(currentTrack.getTitle());
        info.setArtist(currentTrack.getArtist());
        info.setCoverUrl("/covers?trackId=" + currentTrack.getId());
        return info;
    }

    public static class NowPlayingInfo {
        private int trackId;
        private String title;
        private String artist;
        private int listeners;
        private String coverUrl;
        // Getters/Setters...
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getArtist() { return artist; }
        public void setArtist(String artist) { this.artist = artist; }
        public int getListeners() { return listeners; }
        public void setListeners(int listeners) { this.listeners = listeners; }
        public String getCoverUrl() { return coverUrl; }
        public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
        public int getTrackId() { return trackId; }
        public void setTrackId(int trackId) { this.trackId = trackId; }
    }
}