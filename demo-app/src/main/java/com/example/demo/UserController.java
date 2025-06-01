package com.example.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    private final Audit audit;

    UserController(UserService service, Audit audit) {
        this.service = service;
        this.audit = audit;
    }

    @GetMapping
    UserService.Results list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "") String q) {
        return service.list(page, size, q);
    }

    @GetMapping("/{id}")
    User get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    User create(@Valid @RequestBody Input input, @RequestAttribute String actor) {
        return service.save(null, input, actor);
    }

    @PutMapping("/{id}")
    User update(@PathVariable long id, @Valid @RequestBody Input input, @RequestAttribute String actor) {
        return service.save(id, input, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable long id, @RequestAttribute String actor) {
        service.delete(id, actor);
    }

    @GetMapping("/export")
    ResponseEntity<byte[]> export(@RequestParam(defaultValue = "") String q, @RequestAttribute String actor)
            throws IOException {
        byte[] bytes = UserExport.write(service.export(q));
        audit.record(actor, "USER_EXPORT", null);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes);
    }

    public record Input(@NotBlank @Size(max = 80) String name, @NotBlank @Email @Size(max = 254) String email,
                        @NotNull User.Status status) {}
}
