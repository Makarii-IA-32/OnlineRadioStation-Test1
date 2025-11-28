package ua.kpi.radio.client.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClientEvents {

    private static final List<Consumer<Integer>> playlistListeners = new ArrayList<>();

    public static void onPlaylistUpdated(Consumer<Integer> listener) {
        playlistListeners.add(listener);
    }

    public static void firePlaylistUpdated(int playlistId) {
        for (Consumer<Integer> listener : playlistListeners) {
            listener.accept(playlistId);
        }
    }
}