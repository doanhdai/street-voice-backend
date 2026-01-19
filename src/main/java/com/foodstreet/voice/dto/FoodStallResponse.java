package com.foodstreet.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodStallResponse {

    private Long id;
    private String name;
    private String description;
    private String audioUrl;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
}