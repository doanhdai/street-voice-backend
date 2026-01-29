package com.foodstreet.voice.controller;

import com.foodstreet.voice.service.VietMapSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminSyncController {

    private final VietMapSyncService vietMapSyncService;

    @PostMapping("/sync-vietmap")
    public ResponseEntity<?> syncVietMapData(@RequestBody Map<String, Object> payload) {
        try {
            // Extract params with defaults
            double lat = payload.containsKey("lat") ? ((Number) payload.get("lat")).doubleValue() : 10.760;
            double lng = payload.containsKey("lng") ? ((Number) payload.get("lng")).doubleValue() : 106.700;
            String keyword = (String) payload.getOrDefault("keyword", "quán ăn");

            log.info("Received admin sync request: lat={}, lng={}, keyword={}", lat, lng, keyword);

            int count = vietMapSyncService.syncStallsFromVietMap(lat, lng, keyword);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "saved_count", count,
                    "message", "Synced " + count + " items from VietMap"));
        } catch (Exception e) {
            log.error("Sync failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }
}
