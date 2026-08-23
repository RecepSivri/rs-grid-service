package com.rs.gridservice.service;

import com.rs.gridservice.dto.UserCreateRequest;
import com.rs.gridservice.dto.UserResponse;
import com.rs.gridservice.dto.UserUpdateRequest;

import java.util.List;

/**
 * Keycloak Admin API'sini wrap eden kullanici yonetim servisi.
 * add, delete, edit, get, getAll operasyonlarini saglar.
 */
public interface UserService {

    UserResponse addUser(UserCreateRequest request);

    UserResponse getUser(String userId);

    List<UserResponse> getAllUsers(int first, int max, String search);

    UserResponse editUser(String userId, UserUpdateRequest request);

    void deleteUser(String userId);
}
