package org.example.care.dto;

import org.example.care.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String message;
    private String username;
    private Role role;
}
