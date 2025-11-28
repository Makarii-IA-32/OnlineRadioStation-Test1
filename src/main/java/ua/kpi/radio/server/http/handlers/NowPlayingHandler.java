package ua.kpi.radio.server.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ua.kpi.radio.service.RadioService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class NowPlayingHandler implements HttpHandler {

    private final RadioService radioService = RadioService.getInstance();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        int channelId = 1;
        String userId = null; // ЗМІНА: за замовчуванням null

        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String p : query.split("&")) {
                String[] kv = p.split("=");
                if (kv.length == 2) {
                    if (kv[0].equals("channelId")) {
                        try { channelId = Integer.parseInt(kv[1]); } catch (NumberFormatException ignored) {}
                    } else if (kv[0].equals("userId")) {
                        userId = kv[1];
                    }
                }
            }
        }

        // ЗМІНА: Реєструємо активність ТІЛЬКИ якщо є userId.
        // Адмінка не шле userId, тому вона не буде рахуватися.
        if (userId != null && !userId.isBlank()) {
            radioService.registerHeartbeat(channelId, userId);
        }

        var info = radioService.getNowPlayingInfo(channelId);

        String json = gson.toJson(info);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}