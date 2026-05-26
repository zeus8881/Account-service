package com.example.myspringproject.service;

import com.example.myspringproject.dto.*;
import com.example.myspringproject.entity.Role;
import com.example.myspringproject.entity.RoleName;
import com.example.myspringproject.entity.User;
import com.example.myspringproject.repository.RoleRepository;
import com.example.myspringproject.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.myspringproject.entity.ActionName.*;
import static com.example.myspringproject.entity.Operation.LOCK;
import static com.example.myspringproject.entity.Operation.UNLOCK;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final SecurityEventService securityEventService;
    private final Set<String> badPasswords = Set.of(
            "PasswordForJanuary",
            "PasswordForFebruary",
            "PasswordForMarch",
            "PasswordForApril",
            "PasswordForMay",
            "PasswordForJune",
            "PasswordForJuly",
            "PasswordForAugust",
            "PasswordForSeptember",
            "PasswordForOctober",
            "PasswordForNovember",
            "PasswordForDecember");

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository, SecurityEventService securityEventService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.securityEventService = securityEventService;
    }

    public SignupResponseDTO signUp(SignupRequestDTO signupRequestDTO) {
        if (!signupRequestDTO.email().endsWith("@acme.com")) {
            throw new IllegalArgumentException("Invalid employee");
        }

        if (userRepository.findByEmail(signupRequestDTO.email().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        } else {

            if (signupRequestDTO.password().length() < 12) {
                throw new IllegalArgumentException("Password must be at least 12 characters long");

            } else if (badPasswords.contains(signupRequestDTO.password())) {
                throw new IllegalArgumentException("The password is in the hacker's database!");

            }

            Role admin = roleRepository.findByName(RoleName.ROLE_ADMINISTRATOR);
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER);

            if (admin == null || userRole == null) {
                throw new RuntimeException("Roles are not set up properly.");
            }

            User user = User.builder()
                    .name(signupRequestDTO.name())
                    .lastName(signupRequestDTO.lastName())
                    .email(signupRequestDTO.email().toLowerCase())
                    .password(passwordEncoder.encode(signupRequestDTO.password()))
                    .build();

            user.setRoles(new HashSet<>());

            if (userRepository.count() == 0) {
                user.getRoles().add(admin);
                User savedUser = userRepository.save(user);

                securityEventService.eventSecurity(
                        LocalDateTime.now(),
                        CREATE_USER,
                        "Anonymous",
                        savedUser.getEmail(),
                        "/api/auth/signup"
                );

                return new SignupResponseDTO(savedUser.getId(),
                        savedUser.getName(),
                        savedUser.getLastName(),
                        savedUser.getEmail()
                                .toLowerCase());

            } else {
                user.getRoles().add(userRole);
                User savedUser = userRepository.save(user);

                securityEventService.eventSecurity(
                        LocalDateTime.now(),
                        CREATE_USER,
                        "Anonymous",
                        savedUser.getEmail(),
                        "/api/auth/signup"
                );

                return new SignupResponseDTO(savedUser.getId(),
                        savedUser.getName(),
                        savedUser.getLastName(),
                        savedUser.getEmail().toLowerCase());
            }
        }
    }

    public SignupResponseDTO getApiPayment(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

        return new SignupResponseDTO(user.getId(),
                user.getName(),
                user.getLastName(),
                user.getEmail()
                        .toLowerCase());
    }

    public ChangePasswordResponse changePswd(Principal principal, ChangePasswordRequest passwordRequestDTO) {
        String email = principal.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException(email));

        if (passwordRequestDTO.newPassword().length() < 12) {
            throw new IllegalArgumentException("Password must be at least 12 characters long");

        } else if (badPasswords.contains(passwordRequestDTO.newPassword())) {
            throw new IllegalArgumentException("The password is in the hacker's database!");

        } else if (passwordEncoder.matches(passwordRequestDTO.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current password");

        } else {

            user.setPassword(passwordEncoder.encode(passwordRequestDTO.newPassword()));
            securityEventService.eventSecurity(
                    LocalDateTime.now(),
                    CHANGE_PASSWORD,
                    user.getEmail(),
                    user.getEmail(),
                    "/api/auth/changepass"
            );

            User save = userRepository.save(user);

            return new ChangePasswordResponse(save.getEmail(),
                    "The password has been updated successfully");
        }
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(user ->
                        new UserResponse(user.getId(),
                                user.getName(),
                                user.getLastName(),
                                user.getEmail(),
                                user.getRoles().stream()
                                        .map(role ->
                                                role.getName()
                                                        .name())
                                        .sorted()
                                        .toList()))
                .toList();
    }

    public UserDeleteResponse deleteUser(Principal principal, String email) {
        User deletedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        if (deletedUser.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMINISTRATOR))) {
            throw new IllegalArgumentException("Can't remove ADMINISTRATOR role!");
        } else if (deletedUser.getEmail().equals(principal.getName())) {
            throw new IllegalArgumentException("Can't remove your own account!");
        }
        userRepository.delete(deletedUser);

        securityEventService.eventSecurity(
                LocalDateTime.now(),
                DELETE_USER,
                principal.getName(),
                deletedUser.getEmail(),
                "/api/admin/user"
        );

        return new UserDeleteResponse(deletedUser.getEmail(),
                "Deleted successfully!");
    }

    public UserResponse changeRole(UserRequest userRequest, Principal principal) {
        User user = userRepository.findByEmail(userRequest.user())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Role targetRole = roleRepository.findByName(RoleName.valueOf(userRequest.role().toUpperCase()));


        if (targetRole == null) {
            throw new IllegalArgumentException("Invalid role");
        }

        Set<Role> roles = new HashSet<>(user.getRoles());

        if (userRequest.operation().equals("GRANT")) {

            if (targetRole.getName().equals(RoleName.ROLE_ADMINISTRATOR)) {
                if (!roles.isEmpty()) {
                    throw new IllegalArgumentException("User does not have other roles than ADMINISTRATOR");
                }
            }

            if (!targetRole.getName().equals(RoleName.ROLE_ADMINISTRATOR)) {
                if (user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMINISTRATOR))) {
                    throw new IllegalArgumentException("User does not have role ADMINISTRATOR");
                }
            }
            roles.add(targetRole);

            securityEventService.eventSecurity(
                    LocalDateTime.now(),
                    GRANT_ROLE,
                    principal.getName(),
                    "Grant role " + targetRole.getName() + " to: " + user.getEmail(),
                    "/api/admin/user/role"
            );
        }

        if (userRequest.operation().equals("REMOVE")) {

            if (user.getRoles().stream().noneMatch(role -> role.getName().equals(targetRole.getName()))) {
                throw new IllegalArgumentException("User does not have this role");
            }

            if (targetRole.getName().equals(RoleName.ROLE_ADMINISTRATOR)) {
                throw new IllegalArgumentException("Wrong! ADMINISTRATOR role cannot be removed");
            }

            if (roles.size() == 1) {
                throw new IllegalArgumentException("User must have at least one role");
            }
            roles.remove(targetRole);

            securityEventService.eventSecurity(
                    LocalDateTime.now(),
                    REMOVE_ROLE,
                    principal.getName(),
                    "Remove role " + targetRole.getName() + " from: " + user.getEmail(),
                    "/api/admin/user/role"
            );
        }

        user.setRoles(roles);
        userRepository.save(user);

        return new UserResponse(user.getId(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                roles.stream().map(role -> role.getName()
                                .name())
                        .sorted()
                        .toList());
    }

    public UserAccessResponse userAdminAccessUserDTO(
            UserAccessRequest userAccessRequest,
            Principal principal) {

        User user = userRepository.findByEmail(userAccessRequest.user().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMINISTRATOR))) {
            throw new IllegalArgumentException("Can't lock the ADMINISTRATOR!");
        }

        if (userAccessRequest.operation().equals(LOCK)) {
            user.setLocked(true);
            user.setFailedAttempts(0);
            userRepository.save(user);

            securityEventService.eventSecurity(
                    LocalDateTime.now(),
                    LOCK_USER,
                    principal.getName(),
                    "Lock user " + user.getEmail(),
                    "/api/admin/user/access"
            );

        } else if (userAccessRequest.operation().equals(UNLOCK)) {
            user.setLocked(false);
            user.setFailedAttempts(0);
            userRepository.save(user);

            securityEventService.eventSecurity(
                    LocalDateTime.now(),
                    UNLOCK_USER,
                    principal.getName(),
                    "Unlock user " + user.getEmail(),
                    "/api/admin/user/access"
            );

        } else {
            throw new IllegalArgumentException("Invalid operation");
        }
        return new UserAccessResponse(
                "User " + user.getEmail() + " " + userAccessRequest.operation().name() + "!"
        );
    }
}