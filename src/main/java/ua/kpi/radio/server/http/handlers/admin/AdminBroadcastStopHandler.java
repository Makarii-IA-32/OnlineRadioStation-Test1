package ua.kpi.radio.server.http.handlers.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ua.kpi.radio.radio.RadioChannelManager;

import java.io.IOException;
import java.net.URI;

public class AdminBroadcastStopHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String idStr = getQueryParam(exchange.getRequestURI(), "id");

        try {
            if (idStr != null) {
                int id = Integer.parseInt(idStr);
                RadioChannelManager.getInstance().stopChannel(id);
            } else {
                // Якщо ID не передано — зупиняємо все
                RadioChannelManager.getInstance().stopAllChannels();
            }
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close(); // <--- ВАЖЛИВО

        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().close(); // <--- ВАЖЛИВО
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
            exchange.getResponseBody().close(); // <--- ВАЖЛИВО
        }
    }

    private String getQueryParam(URI uri, String key) {
        String query = uri.getQuery();
        if (query == null) return null;
        for (String p : query.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }
}