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

    @PostMapping("/register")
    public Doctor register(@AuthenticationPrincipal Jwt jwt,
                           @RequestBody(required = false) Doctor body) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "В токене нет логина (preferred_username)");
        }
        Optional<Doctor> existing = repository.findByUsernameIgnoreCase(username);
        if (existing.isPresent()) {
            return existing.get();
        }
        Doctor doctor = new Doctor();
        doctor.setUsername(username);
        doctor.setName(body != null && body.getName() != null ? body.getName() : username);
        doctor.setSpecialty(body != null && body.getSpecialty() != null ? body.getSpecialty() : "Врач");
        Doctor saved = repository.save(doctor);
        log.info("Создан профиль врача: id={}, username={}", saved.getId(), username);
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
