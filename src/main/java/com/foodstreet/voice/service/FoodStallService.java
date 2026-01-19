package com.foodstreet.voice.service;

import com.foodstreet.voice.dto.CreateFoodStallRequest;
import com.foodstreet.voice.dto.FoodStallResponse;
import com.foodstreet.voice.dto.UpdateFoodStallRequest;
import com.foodstreet.voice.entity.FoodStall;
import com.foodstreet.voice.exception.ResourceNotFoundException;
import com.foodstreet.voice.repository.FoodStallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodStallService {

    private final FoodStallRepository foodStallRepository;
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional(readOnly = true)
    public List<FoodStallResponse> getAllStalls() {
        log.debug("Fetching all food stalls");
        List<FoodStall> stalls = foodStallRepository.findAll();
        log.debug("Found {} food stalls", stalls.size());
        return stalls.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FoodStallResponse getStallById(Long id) {
        log.debug("Fetching food stall with id: {}", id);
        FoodStall stall = foodStallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food stall not found with id: " + id));
        return mapToResponse(stall);
    }

    @Transactional(readOnly = true)
    public FoodStallResponse findNearestStall(double latitude, double longitude) {
        log.debug("Finding nearest stall to coordinates: lat={}, lon={}", latitude, longitude);

        FoodStall stall = foodStallRepository.findNearestStall(latitude, longitude)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No food stall found near the given location"));

        log.debug("Found nearest stall: {}", stall.getName());

        return mapToResponse(stall);
    }

    @Transactional
    public FoodStallResponse createStall(CreateFoodStallRequest request) {
        log.debug("Creating new food stall: {}", request.getName());

                Point location = geometryFactory.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude()));

        FoodStall stall = FoodStall.builder()
                .name(request.getName())
                .description(request.getDescription())
                .audioUrl(request.getAudioUrl())
                .imageUrl(request.getImageUrl())
                .location(location)
                .build();

        FoodStall savedStall = foodStallRepository.save(stall);
        log.debug("Created food stall with id: {}", savedStall.getId());

        return mapToResponse(savedStall);
    }

    @Transactional
    public FoodStallResponse updateStall(Long id, UpdateFoodStallRequest request) {
        log.debug("Updating food stall with id: {}", id);

        FoodStall stall = foodStallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food stall not found with id: " + id));

                if (request.getName() != null) {
            stall.setName(request.getName());
        }
        if (request.getDescription() != null) {
            stall.setDescription(request.getDescription());
        }
        if (request.getAudioUrl() != null) {
            stall.setAudioUrl(request.getAudioUrl());
        }
        if (request.getImageUrl() != null) {
            stall.setImageUrl(request.getImageUrl());
        }
        if (request.getLatitude() != null && request.getLongitude() != null) {
            Point location = geometryFactory.createPoint(
                    new Coordinate(request.getLongitude(), request.getLatitude()));
            stall.setLocation(location);
        }

        FoodStall updatedStall = foodStallRepository.save(stall);
        log.debug("Updated food stall: {}", updatedStall.getName());

        return mapToResponse(updatedStall);
    }

    @Transactional
    public void deleteStall(Long id) {
        log.debug("Deleting food stall with id: {}", id);

        if (!foodStallRepository.existsById(id)) {
            throw new ResourceNotFoundException("Food stall not found with id: " + id);
        }

        foodStallRepository.deleteById(id);
        log.debug("Deleted food stall with id: {}", id);
    }

    private FoodStallResponse mapToResponse(FoodStall stall) {
        return FoodStallResponse.builder()
                .id(stall.getId())
                .name(stall.getName())
                .description(stall.getDescription())
                .audioUrl(stall.getAudioUrl())
                .imageUrl(stall.getImageUrl())
                .latitude(stall.getLocation().getY())                 .longitude(stall.getLocation().getX())                 .build();
    }
}