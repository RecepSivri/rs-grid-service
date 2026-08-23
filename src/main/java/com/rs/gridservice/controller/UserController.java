package com.rs.gridservice.controller;

import com.rs.gridservice.dto.UserCreateRequest;
import com.rs.gridservice.dto.UserResponse;
import com.rs.gridservice.dto.UserUpdateRequest;
import com.rs.gridservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * rs-grid-service'in disariya actigi kullanici yonetim API'leri.
 * Arka planda Keycloak Admin REST API'sini wrap eder.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Yeni kullanici ekle (add). */
    @PostMapping
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.addUser(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.getId())).body(created);
    }

    /** Tek bir kullaniciyi getir (get). */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    /** Tum kullanicilari listele, opsiyonel sayfalama ve arama (getAll). */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "50") int max,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(userService.getAllUsers(first, max, search));
    }

    /** Kullaniciyi guncelle (edit). */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> editUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.editUser(userId, request));
    }

    /** Kullaniciyi sil (delete). */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
