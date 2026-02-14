package com.foodstreet.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodStallImportDto {
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private String description;
    private String audioUrl;
    private Integer triggerRadius;
    private String priceRange;
    private String featuredReview;
    private Double rating;
}
