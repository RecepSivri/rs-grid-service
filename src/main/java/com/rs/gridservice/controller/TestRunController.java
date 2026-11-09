package com.rs.gridservice.controller;

import com.rs.gridservice.dto.TestRunResponse;
import com.rs.gridservice.dto.TestRunUpsertRequest;
import com.rs.gridservice.service.TestRunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * "Tümünü Çalıştır" anlik goruntulerini (Smoke/Regresyon, proje basina en son
 * calistirma) yonetir. GET, bir kullanicinin TUM projelerinin TUM
 * anlik goruntulerini tek cagrida getirir (bkz. Dashboard "Test Durumu"
 * paneli). PUT, TEK bir proje+modun anlik goruntusunu bastan yazar. Flat bir
 * ust-duzey kaynak oldugu icin (orn. "/api/v1/projects/**" altinda degil)
 * SecurityConfig'e kendi kurali eklendi (bkz. yorum orada).
 */
@RestController
@RequestMapping("/api/v1/test-runs")
@RequiredArgsConstructor
public class TestRunController {

    private final TestRunService testRunService;

    @GetMapping
    public ResponseEntity<List<TestRunResponse>> getRunsForUser(@RequestParam String userId) {
        return ResponseEntity.ok(testRunService.getRunsForUser(userId));
    }

    @PutMapping("/{mode}")
    public ResponseEntity<TestRunResponse> replaceRun(
            @PathVariable String mode,
            @RequestParam String projectId,
            @RequestParam String userId,
            @Valid @RequestBody TestRunUpsertRequest request) {
        return ResponseEntity.ok(testRunService.replaceRun(projectId, mode, userId, request));
    }
}
