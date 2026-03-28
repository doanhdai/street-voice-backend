package com.foodstreet.voice.service;

import com.foodstreet.voice.auth.entity.User;
import com.foodstreet.voice.auth.entity.UserRole;
import com.foodstreet.voice.auth.repository.UserRepository;
import com.foodstreet.voice.dto.stall.StallOwnerUpsertRequest;
import com.foodstreet.voice.entity.FoodStall;
import com.foodstreet.voice.entity.FoodStallUpdate;
import com.foodstreet.voice.entity.FoodStallUpdateStatus;
import com.foodstreet.voice.entity.StallStatus;
import com.foodstreet.voice.exception.ResourceNotFoundException;
import com.foodstreet.voice.repository.FoodStallRepository;
import com.foodstreet.voice.repository.FoodStallUpdateRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StallOwnerService {

    private final UserRepository userRepository;
    private final FoodStallRepository foodStallRepository;
    private final FoodStallUpdateRepository foodStallUpdateRepository;

    @Transactional(readOnly = true)
    public FoodStall getMyStall(String username) {
        User owner = findOwner(username);
        if (owner.getRestaurantId() == null) {
            return null;
        }

        return foodStallRepository.findByIdAndOwnerId(owner.getRestaurantId(), owner.getId())
                .orElse(null);
    }

    @Transactional
    public FoodStall submitStallUpdate(String username, StallOwnerUpsertRequest request) {
        User owner = findOwner(username);
        FoodStall stall;
        FoodStallUpdateStatus pendingStatus;

        if (owner.getRestaurantId() == null) {
            // New stall registration request:
            // Do NOT create a FoodStall record yet. Only store the request in food_stall_updates.
            stall = null;
            pendingStatus = FoodStallUpdateStatus.CREATE_PENDING;
        } else {
            stall = foodStallRepository.findByIdAndOwnerId(owner.getRestaurantId(), owner.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stall does not exist for this owner"));

            if (stall.getStatus() == StallStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Your stall update is pending approval. You cannot edit until admin reviews it.");
            }

            stall.setStatus(StallStatus.PENDING);
            foodStallRepository.save(stall);
            pendingStatus = hasApprovedUpdate(stall.getId())
                    ? FoodStallUpdateStatus.UPDATE_PENDING
                    : FoodStallUpdateStatus.CREATE_PENDING;
        }

        Map<String, Object> changes = toChanges(request);
        FoodStallUpdate update = FoodStallUpdate.builder()
                .foodStall(stall)
                .owner(owner)
                .status(pendingStatus)
                .changes(changes)
                .build();
        foodStallUpdateRepository.save(update);

        return stall;
    }

    private boolean hasApprovedUpdate(Long stallId) {
        return foodStallUpdateRepository.existsByFoodStall_IdAndStatus(
                stallId,
                FoodStallUpdateStatus.APPROVED
        );
    }

    private Map<String, Object> toChanges(StallOwnerUpsertRequest request) {
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("name", request.getName());
        changes.put("description", request.getDescription());
        changes.put("address", request.getAddress());
        changes.put("latitude", request.getLatitude());
        changes.put("longitude", request.getLongitude());
        changes.put("minPrice", request.getMinPrice());
        changes.put("maxPrice", request.getMaxPrice());
        changes.put("triggerRadius", request.getTriggerRadius());
        return changes;
    }

    private User findOwner(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (owner.getRole() != UserRole.RESTAURANT_OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only restaurant owner can access this endpoint");
        }

        return owner;
    }
}
