-- Таблиця треків
CREATE TABLE IF NOT EXISTS tracks (
                                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      title TEXT NOT NULL,
                                      artist TEXT,
                                      album TEXT,
                                      audio_path TEXT NOT NULL,
                                      cover_path TEXT
);

-- Користувачі
CREATE TABLE IF NOT EXISTS users (
                                     id          INTEGER PRIMARY KEY AUTOINCREMENT,
                                     display_name TEXT,
                                     session_id  TEXT UNIQUE
);

-- Таблиця каналів
CREATE TABLE IF NOT EXISTS radio_channels (
                                              id          INTEGER PRIMARY KEY AUTOINCREMENT,
                                              name        TEXT NOT NULL UNIQUE,
                                              playlist_id INTEGER,
                                              bitrate     INTEGER DEFAULT 128
);

--
CREATE TABLE IF NOT EXISTS channel_stats (
                                             id             INTEGER PRIMARY KEY AUTOINCREMENT,
                                             channel_id     INTEGER NOT NULL,
                                             listener_count INTEGER NOT NULL,
                                             recorded_at    TEXT NOT NULL,  -- Час запису статистики
                                             FOREIGN KEY (channel_id) REFERENCES radio_channels(id)
);

-- Плейлисти
CREATE TABLE IF NOT EXISTS playlists (
                                         id          INTEGER PRIMARY KEY AUTOINCREMENT,
                                         name        TEXT NOT NULL
);

-- Звʼязок плейлистів і треків
CREATE TABLE IF NOT EXISTS playlist_tracks (
                                               playlist_id INTEGER NOT NULL,
                                               track_id    INTEGER NOT NULL,
                                               order_index INTEGER NOT NULL,
                                               PRIMARY KEY (playlist_id, track_id),
                                               FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
                                               FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE
);