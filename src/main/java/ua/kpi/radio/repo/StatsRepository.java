package ua.kpi.radio.repo;
import java.sql.SQLException;
import java.util.Map;

public interface StatsRepository {
    void saveStat(int channelId, int count) throws SQLException;

    Map<String, Double> getHourlyStats(int channelId) throws SQLException;
}