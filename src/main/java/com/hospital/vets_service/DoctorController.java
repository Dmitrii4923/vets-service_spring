package com.hospital.vets_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vets")
public class DoctorController {
    private static final Logger log = LoggerFactory.getLogger(DoctorController.class);

    private final DoctorRepository repository;

    public DoctorController(DoctorRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Doctor> getAll() {
        log.debug("Запрошен список врачей");
        return repository.findAll();
    }

    @PostMapping
    public Doctor create(@RequestBody Doctor doctor) {
        log.info("Создание врача: username={}", doctor.getUsername());
        Doctor saved = repository.save(doctor);
        log.info("Врач создан: id={}", saved.getId());
        return saved;
    }

    @GetMapping("/me")
    public Doctor me(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        log.debug("Запрошен профиль врача: username={}", username);

        return (username == null
                ? Optional.<Doctor>empty()
                : repository.findByUsernameIgnoreCase(username))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Профиль врача не привязан к учётной записи"));
    }

    @GetMapping("/{id}")
    public Doctor getById(@PathVariable Long id) {
        log.debug("Запрошен врач: id={}", id);
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Врач не найден"));
    }
}
