package com.banking.bffservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
