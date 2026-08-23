package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Keycloak UserRepresentation'in disariya acilan sadelestirilmis hali.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean enabled;
    private Boolean emailVerified;
    private Long createdTimestamp;
    /** admin / web / desktop realm rolunden turetilir; kullanicinin bu roller disinda bir rolu yoksa null. */
    private UserType userType;
}
