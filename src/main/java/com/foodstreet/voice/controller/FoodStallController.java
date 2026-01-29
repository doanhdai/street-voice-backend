package com.foodstreet.voice.controller;

import com.foodstreet.voice.dto.CreateFoodStallRequest;
import com.foodstreet.voice.dto.FoodStallResponse;
import com.foodstreet.voice.dto.NearbyRequest;
import com.foodstreet.voice.dto.UpdateFoodStallRequest;
import com.foodstreet.voice.entity.FoodStall;
import com.foodstreet.voice.repository.FoodStallRepository;
import com.foodstreet.voice.service.AudioService;
import com.foodstreet.voice.service.FoodStallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stalls")
@RequiredArgsConstructor
@Validated
@Slf4j
public class FoodStallController {

    private final FoodStallService foodStallService;

    @GetMapping
    public ResponseEntity<List<FoodStallResponse>> getAllStalls() {
        log.info("Received request to get all food stalls");
        List<FoodStallResponse> stalls = foodStallService.getAllStalls();
        log.info("Returning {} food stalls", stalls.size());
        return ResponseEntity.ok(stalls);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodStallResponse> getStallById(@PathVariable Long id) {
        log.info("Received request to get food stall with id: {}", id);
        FoodStallResponse stall = foodStallService.getStallById(id);
        return ResponseEntity.ok(stall);
    }

    @GetMapping("/nearby")
    public ResponseEntity<FoodStallResponse> findNearestStall(@Valid @ModelAttribute NearbyRequest request) {
        log.info("Received request to find nearest stall: lat={}, lon={}", request.getLat(), request.getLon());

        FoodStallResponse response = foodStallService.findNearestStall(
                request.getLat(),
                request.getLon());

        log.info("Returning nearest stall: {}", response.getName());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FoodStallResponse> createStall(@Valid @RequestBody CreateFoodStallRequest request) {
        log.info("Received request to create food stall: {}", request.getName());
        FoodStallResponse stall = foodStallService.createStall(request);
        log.info("Created food stall with id: {}", stall.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(stall);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodStallResponse> updateStall(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFoodStallRequest request) {
        log.info("Received request to update food stall with id: {}", id);
        FoodStallResponse stall = foodStallService.updateStall(id, request);
        log.info("Updated food stall: {}", stall.getName());
        return ResponseEntity.ok(stall);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStall(@PathVariable Long id) {
        log.info("Received request to delete food stall with id: {}", id);
        foodStallService.deleteStall(id);
        log.info("Deleted food stall with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Autowired
    private AudioService audioService;

    @Autowired
    private FoodStallRepository foodStallRepository;

    // API Sync cho Mobile (Offline)
    @GetMapping("/sync")
    public ResponseEntity<List<FoodStallResponse>> syncDataForMobile(
            @RequestParam(defaultValue = "10.762622") double lat, // Toa do Q4
            @RequestParam(defaultValue = "106.700174") double lng,
            @RequestParam(defaultValue = "2000") double radius) { // R=2km

        // Lay tat ca cac quan Q4 voi R=2km
        List<FoodStall> stalls = foodStallRepository.findStallsWithinRadius(lat, lng, radius);

        //Mapping
        List<FoodStallResponse> response = stalls.stream().map(stall -> {
            FoodStallResponse res = convertToResponse(stall);

            // Lazy gen
            if (res.getAudioUrl() == null || res.getAudioUrl().isEmpty()) {
                String audioUrl = audioService.getOrCreateAudio(
                        "Xin chao day la " + stall.getName() + ". " + stall.getDescription(),
                        "vi"
                );
                res.setAudioUrl(audioUrl);
            }
            return res;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private FoodStallResponse convertToResponse(FoodStall stall) {
        FoodStallResponse response = new FoodStallResponse();

        response.setId(stall.getId());
        response.setName(stall.getName());
        response.setDescription(stall.getDescription());

        // Map path
        response.setAudioUrl(stall.getAudioUrl());
        response.setImageUrl(stall.getImageUrl());

        // QUAN TRỌNG: Chuyển đổi tọa độ từ PostGIS (Point) sang Lat/Lng
        // Vì Mobile App (Flutter/React Native) chỉ hiểu Lat/Lng, không hiểu Geometry Object
        if (stall.getLocation() != null) {
            response.setLatitude(stall.getLocation().getY());  // Y là Vĩ độ (Lat)
            response.setLongitude(stall.getLocation().getX()); // X là Kinh độ (Lng)
        }
        return response;
    }

}