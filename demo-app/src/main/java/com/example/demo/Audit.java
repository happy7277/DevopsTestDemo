package com.example.demo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Audit {
    private final JdbcTemplate db;

    Audit(JdbcTemplate db) {
        this.db = db;
    }

    public void record(String actor, String action, Long targetId) {
        db.update("INSERT INTO audit_logs(actor, action, target_id, created_at) VALUES (?, ?, ?, ?)",
            actor, action, targetId, LocalDateTime.now(ZoneOffset.UTC));
    }

    public List<Entry> latest(int limit) {
        return db.query("SELECT id, actor, action, target_id, created_at FROM audit_logs ORDER BY id DESC LIMIT ?",
            (row, index) -> new Entry(row.getLong("id"), row.getString("actor"), row.getString("action"),
                row.getObject("target_id", Long.class), row.getObject("created_at", LocalDateTime.class)), limit);
    }

    public record Entry(long id, String actor, String action, Long targetId, LocalDateTime createdAt) {}
}
