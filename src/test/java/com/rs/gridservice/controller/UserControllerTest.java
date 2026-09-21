package com.rs.gridservice.controller;

import com.rs.gridservice.dto.UserCreateRequest;
import com.rs.gridservice.dto.UserResponse;
import com.rs.gridservice.dto.UserType;
import com.rs.gridservice.dto.UserUpdateRequest;
import com.rs.gridservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void addUserReturnsCreatedWithLocation() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("ahmet.yilmaz");
        request.setUserType(UserType.WEB);
        UserResponse created = UserResponse.builder().id("user-1").username("ahmet.yilmaz").build();
        when(userService.addUser(request)).thenReturn(created);

        ResponseEntity<UserResponse> response = userController.addUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/users/user-1");
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void getUserReturnsUserFromService() {
        UserResponse user = UserResponse.builder().id("user-1").build();
        when(userService.getUser("user-1")).thenReturn(user);

        ResponseEntity<UserResponse> response = userController.getUser("user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(user);
    }

    @Test
    void getAllUsersDelegatesPagingAndSearchToService() {
        List<UserResponse> users = List.of(UserResponse.builder().id("user-1").build());
        when(userService.getAllUsers(0, 20, "ahmet")).thenReturn(users);

        ResponseEntity<List<UserResponse>> response = userController.getAllUsers(0, 20, "ahmet");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(users);
    }

    @Test
    void editUserReturnsUpdatedUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Ahmet Can");
        UserResponse updated = UserResponse.builder().id("user-1").firstName("Ahmet Can").build();
        when(userService.editUser("user-1", request)).thenReturn(updated);

        ResponseEntity<UserResponse> response = userController.editUser("user-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updated);
    }

    @Test
    void deleteUserReturnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = userController.deleteUser("user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteUser("user-1");
    }
}
