package com.foodstreet.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodStallResponse {
    private Long id;
    private String name;
    private String address;
    private String description;
    @Schema(description = "Latitude of the food stall location", example = "10.762622")
    private Double latitude;
    @Schema(description = "Longitude of the food stall location", example = "106.700174")
    private Double longitude;
    private Integer triggerRadius;
    @Schema(description = "URL of the generated audio guide for offline playback", example = "https://storage.example.com/audio/stall_123.mp3")
    private String audioUrl;
    private String imageUrl;
    private String priceRange;
    private String featuredReview;
    private Double rating;
}