package com.example.demo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserMapper users;
    private final Audit audit;
    private Page<User> selectedPage;

    UserService(UserMapper users, Audit audit) {
        this.users = users;
        this.audit = audit;
    }

    public Results list(int page, int size, String q) {
        if (page < 1 || size < 1 || size > 100 || q.length() > 254) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination or search");
        }
        selectedPage = users.selectPage(new Page<>(page, size), search(q));
        return new Results(selectedPage.getRecords(), selectedPage.getTotal(), page, size);
    }

    public List<User> export(String q) {
        if (q.length() > 254) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid search");
        var result = users.selectPage(new Page<>(1, 10_001, false), search(q)).getRecords();
        if (result.size() > 10_000) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Export exceeds 10000 rows");
        }
        return result;
    }

    public User get(long id) {
        var result = users.selectById(id);
        if (result == null) throw missing();
        return result;
    }

    @Transactional
    public User save(Long id, UserController.Input input, String actor) {
        var user = id == null ? new User() : get(id);
        user.name = input.name().trim();
        user.email = input.email().toLowerCase(Locale.ROOT);
        user.status = input.status();
        if (id == null) {
            user.createdAt = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
            users.insert(user);
        } else if (users.updateById(user) == 0) {
            throw missing();
        }
        audit.record(actor, id == null ? "USER_CREATE" : "USER_UPDATE", user.id);
        return user;
    }

    @Transactional
    public void delete(long id, String actor) {
        if (users.deleteById(id) == 0) throw missing();
        audit.record(actor, "USER_DELETE", id);
    }

    private QueryWrapper<User> search(String q) {
        var wrapper = new QueryWrapper<User>().orderByAsc("id");
        if (!q.isBlank()) wrapper.and(w -> w.like("name", q).or().like("email", q));
        return wrapper;
    }

    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    public record Results(List<User> items, long total, int page, int size) {}
}
