package com.banking.bffservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<Account> accounts;
}
