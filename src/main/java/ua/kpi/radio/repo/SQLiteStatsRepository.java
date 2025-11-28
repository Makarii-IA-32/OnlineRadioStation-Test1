package ua.kpi.radio.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class SQLiteStatsRepository implements StatsRepository {

    @Override
    public void saveStat(int channelId, int count) throws SQLException {
        String sql = "INSERT INTO channel_stats (channel_id, listener_count, recorded_at) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, channelId);
            ps.setInt(2, count);
            ps.setString(3, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    @Override
    public Map<String, Double> getHourlyStats(int channelId) throws SQLException {
        // ЗМІНА: Групуємо по 10 хвилин (беремо 15 символів і додаємо '0')
        // Наприклад: '2023-11-28T14:2' + '0' -> '2023-11-28T14:20'
        String sql = """
            SELECT (substr(recorded_at, 1, 15) || '0') as time_bucket, 
                   AVG(listener_count) as avg_listeners
            FROM channel_stats
            WHERE channel_id = ? 
            GROUP BY time_bucket
            ORDER BY time_bucket ASC 
            LIMIT 30
        """;

        // Використовуємо LinkedHashMap, щоб зберегти хронологічний порядок
        Map<String, Double> result = new LinkedHashMap<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, channelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String fullTime = rs.getString("time_bucket");

                    // Вирізаємо тільки час "HH:mm" для краси на графіку
                    // Формат у базі: YYYY-MM-DDTHH:mm... (T на 10-й позиції, час починається з 11)
                    String shortTime = fullTime;
                    if (fullTime.length() >= 16) {
                        shortTime = fullTime.substring(11, 16); // Витягує "14:20"
                    }

                    result.put(shortTime, rs.getDouble("avg_listeners"));
                }
            }
        }
        return result;
    }
}