package com.foodstreet.voice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceActivityBatchRequest {

    @Schema(description = "Unique device identifier", example = "fcm_token_123")
    private String deviceId;

    @Schema(description = "List of activity counts grouped by stall")
    private List<StallActivityCount> stalls;
}
