package com.foodstreet.voice.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class StallOwnerRegistrationRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Schema(example = "stall_owner_1", description = "Username (3-100 characters)")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(example = "owner@stall.local", description = "Valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Schema(example = "SecurePass@123", description = "Password (8-128 characters, must contain letters and numbers)")
    private String password;

    @NotBlank(message = "Store name is required")
    @Schema(example = "My Stall", description = "Name of the stall")
    private String storeName;

    @Schema(example = "123 Main Street", description = "Address of the stall")
    private String address;

    @Schema(example = "Our delicious stall...", description = "Short description about the stall")
    private String description;

    @Schema(example = "RESTAURANT_OWNER", description = "User role (ADMIN or RESTAURANT_OWNER)")
    private String role;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
