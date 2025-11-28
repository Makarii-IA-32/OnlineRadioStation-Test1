package ua.kpi.radio.service.broadcasting;

import ua.kpi.radio.domain.Track;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Адаптер для утиліти FFmpeg.
 */
public class FfmpegAdapter implements StreamEncoder {

    private Process ffmpegProcess;

    @Override
    public void stream(Track track, Path outputDir, long seekMs, int bitrate) {
        if (!Files.exists(Path.of(track.getAudioPath()))) {
            System.err.println("File not found: " + track.getAudioPath());
            return;
        }

        File outputM3u8 = outputDir.resolve("stream.m3u8").toFile();

        try {
            ProcessBuilder pb;
            if (seekMs > 0) {
                double seekSeconds = seekMs / 1000.0;
                // Формуємо команду зі зміщенням -ss
                pb = new ProcessBuilder(
                        "ffmpeg", "-hide_banner", "-loglevel", "error",
                        "-ss", String.format("%.3f", seekSeconds).replace(',', '.'),
                        "-re", "-i", track.getAudioPath(),
                        "-vn", "-c:a", "aac",
                        "-b:a", bitrate + "k",
                        "-f", "hls", "-hls_time", "2", "-hls_list_size", "5",
                        "-hls_flags", "delete_segments+append_list+discont_start+omit_endlist",
                        outputM3u8.getAbsolutePath()
                );
            } else {
                // Звичайний запуск
                pb = new ProcessBuilder(
                        "ffmpeg", "-hide_banner", "-loglevel", "error",
                        "-re", "-i", track.getAudioPath(),
                        "-vn", "-c:a", "aac",
                        "-b:a", bitrate + "k",
                        "-f", "hls", "-hls_time", "2", "-hls_list_size", "5",
                        "-hls_flags", "delete_segments+append_list+discont_start+omit_endlist",
                        outputM3u8.getAbsolutePath()
                );
            }

            pb.inheritIO();
            ffmpegProcess = pb.start();
            ffmpegProcess.waitFor();

        } catch (InterruptedException e) {

            stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (ffmpegProcess != null && ffmpegProcess.isAlive()) {
            ffmpegProcess.destroy();
        }
    }

    @Override
    public boolean isAlive() {
        return ffmpegProcess != null && ffmpegProcess.isAlive();
    }
}