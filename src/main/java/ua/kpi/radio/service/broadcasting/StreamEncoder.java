package ua.kpi.radio.service.broadcasting;

import ua.kpi.radio.domain.Track;
import java.nio.file.Path;

/**
 * Інтерфейс для кодувальника потоку (Target в патерні Adapter).
 * Дозволяє підміняти реалізацію (FFmpeg, VLC, GStreamer) без зміни коду транслятора.
 */
public interface StreamEncoder {

    /**
     * Запускає трансляцію треку. Цей метод має бути блокуючим (чекати завершення треку),
     * щоб ChannelBroadcaster міг перейти до наступного треку після завершення.
     */
    void stream(Track track, Path outputDir, long seekMs, int bitrate);

    /**
     * Примусово зупиняє поточну трансляцію.
     */
    void stop();

    /**
     * Перевіряє, чи активний процес кодування.
     */
    boolean isAlive();
}