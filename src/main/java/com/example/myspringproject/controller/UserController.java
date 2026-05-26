package com.example.myspringproject.controller;

import com.example.myspringproject.dto.*;
import com.example.myspringproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<SignupResponseDTO> signUp(@Validated @RequestBody SignupRequestDTO signupRequestDTO) {
        return ResponseEntity.ok(userService.signUp(signupRequestDTO));
    }

    @GetMapping("/empl/payments")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'USER')")
    public ResponseEntity<SignupResponseDTO> apiPayment(Principal principal) {
        return ResponseEntity.ok(userService.getApiPayment(principal));
    }

    @PostMapping("/auth/changepass")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'USER', 'ADMINISTRATOR')")
    public ResponseEntity<ChangePasswordResponse> changePassword(Principal principal, @Validated @RequestBody ChangePasswordRequest newPassword) {
        return ResponseEntity.ok(userService.changePswd(principal, newPassword));
    }

    @GetMapping("/admin/user")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/admin/user/{email}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<UserDeleteResponse> deleteUser(Principal principal, @Validated @PathVariable String email) {
        return ResponseEntity.ok(userService.deleteUser(principal, email));
    }

    @PutMapping("/admin/user/role")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<UserResponse> changeRole(@Validated @RequestBody UserRequest userRequest, Principal principal) {
        return ResponseEntity.ok(userService.changeRole(userRequest, principal));
    }

    @PutMapping("/admin/user/access")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<UserAccessResponse> userAdminAccess(@Validated @RequestBody UserAccessRequest userAccessRequest, Principal principal) {
        return ResponseEntity.ok(userService.userAdminAccessUserDTO(userAccessRequest, principal));
    }
}
