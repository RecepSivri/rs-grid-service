package com.rs.gridservice.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class UserTypeTest {

    @ParameterizedTest
    @EnumSource(UserType.class)
    void roleNameAndToJsonAreLowercaseAndRoundTrip(UserType type) {
        assertThat(type.roleName()).isEqualTo(type.name().toLowerCase(Locale.ROOT));
        assertThat(type.toJson()).isEqualTo(type.roleName());
        assertThat(UserType.fromJson(type.toJson())).isEqualTo(type);
    }

    @Test
    void fromJsonIsCaseInsensitiveAndTrims() {
        assertThat(UserType.fromJson(" admin ")).isEqualTo(UserType.ADMIN);
        assertThat(UserType.fromJson("WEB")).isEqualTo(UserType.WEB);
        assertThat(UserType.fromJson("Desktop")).isEqualTo(UserType.DESKTOP);
    }
}
