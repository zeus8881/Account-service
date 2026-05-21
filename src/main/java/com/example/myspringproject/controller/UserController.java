package com.example.myspringproject.controller;

import com.example.myspringproject.dto.*;
import com.example.myspringproject.service.UserService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ResponseDTO> signUp(@Validated @RequestBody RequestDTO requestDTO) {
        return ResponseEntity.ok(userService.signUp(requestDTO));
    }

    @GetMapping("/empl/payments")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'USER')")
    public ResponseEntity<ResponseDTO> apiPayment(Principal principal) {
        return ResponseEntity.ok(userService.getApiPayment(principal));
    }

    @PostMapping("/auth/changepass")
    public ResponseEntity<ChangePasswordResponseDTO> changePassword(Principal principal, @Validated @RequestBody ChangePasswordRequestDTO newPassword) {
        return ResponseEntity.ok(userService.changePswd(principal, newPassword));
    }

    @GetMapping("/admin/user")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<UserGetResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/admin/user/{email}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<DeleteUserEmailResponseDTO> deleteUser(Principal principal, @Validated @PathVariable String email) {
        return ResponseEntity.ok(userService.deleteUser(principal, email));
    }

    @PutMapping("/admin/user/role")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<UserGetResponseDTO> changeRole(@Validated @RequestBody UserPutRequestDTO userPutRequestDTO, Principal principal) {
        return ResponseEntity.ok(userService.changeRole(userPutRequestDTO, principal));
    }

    @PutMapping("/admin/user/access")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<UserAdminAccessResponseDTO> userAdminAccess(@Validated @RequestBody UserAdminAccessRequestUserDTO userAdminAccessRequestUserDTO, Principal principal) {
        return ResponseEntity.ok(userService.userAdminAccessUserDTO(userAdminAccessRequestUserDTO, principal));
    }
}
