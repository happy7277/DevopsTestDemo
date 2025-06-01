package com.example.demo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TreeMap;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ReportsController {
    private final JdbcTemplate db;
    private final Audit audit;

    ReportsController(JdbcTemplate db, Audit audit) {
        this.db = db;
        this.audit = audit;
    }

    @GetMapping("/dashboard")
    List<Point> dashboard(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        long span = ChronoUnit.DAYS.between(from, to);
        if (from.getYear() < 1000 || to.getYear() > 9998 || span < 0 || span >= 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a range of 1 to 90 days");
        }
        var points = new TreeMap<LocalDate, Point>();
        from.datesUntil(to.plusDays(1)).forEach(day -> points.put(day, new Point(day, 0, 0)));
        db.query("""
            SELECT DATE(created_at) AS day,
                   SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_count,
                   SUM(CASE WHEN status = 'DISABLED' THEN 1 ELSE 0 END) AS disabled_count
            FROM users WHERE created_at >= ? AND created_at < ?
            GROUP BY DATE(created_at)
            """, row -> {
                var day = row.getObject("day", LocalDate.class);
                points.put(day, new Point(day, row.getLong("active_count"), row.getLong("disabled_count")));
            }, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        return List.copyOf(points.values());
    }

    @GetMapping("/audit")
    List<Audit.Entry> audit(@RequestParam(defaultValue = "20") int limit) {
        if (limit < 1 || limit > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid limit");
        return audit.latest(limit);
    }

    record Point(LocalDate date, long active, long disabled) {}
}
