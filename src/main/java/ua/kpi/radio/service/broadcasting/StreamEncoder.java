package ua.kpi.radio.service.broadcasting;

import ua.kpi.radio.domain.Track;
import java.nio.file.Path;


public interface StreamEncoder {

    void stream(Track track, Path outputDir, long seekMs, int bitrate);

    void stop();

    boolean isAlive();
}