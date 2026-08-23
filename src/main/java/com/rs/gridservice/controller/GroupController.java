package com.rs.gridservice.controller;

import com.rs.gridservice.dto.GroupResponse;
import com.rs.gridservice.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * rs-grid-service'in disariya actigi grup listeleme API'si.
 * Arka planda Keycloak Admin REST API'sini wrap eder.
 */
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /** Realmdeki tum gruplari listele, opsiyonel sayfalama ve arama. */
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "50") int max,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(groupService.getAllGroups(first, max, search));
    }
}
