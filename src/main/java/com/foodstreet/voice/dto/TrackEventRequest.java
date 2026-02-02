package com.foodstreet.voice.dto;

import com.foodstreet.voice.entity.UserActivity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackEventRequest {
    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Food Stall ID is required")
    private Long stallId;

    @NotNull(message = "Action type is required")
    private UserActivity.ActionType action;

    private Integer duration; // Optional, in seconds
}
