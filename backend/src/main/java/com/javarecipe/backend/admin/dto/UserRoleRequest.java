package com.javarecipe.backend.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRequest {
    
    @NotEmpty(message = "At least one role is required")
    private List<String> roles;
}
