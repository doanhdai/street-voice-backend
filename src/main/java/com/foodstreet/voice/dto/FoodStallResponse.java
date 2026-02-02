package com.foodstreet.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodStallResponse {
    private Long id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String audioUrl;
    private String imageUrl;
}