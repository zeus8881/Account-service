package com.example.myspringproject.service;

import com.example.myspringproject.dto.*;
import com.example.myspringproject.entity.Operation;
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

    public ResponseDTO signUp(RequestDTO requestDTO) {
        if (!requestDTO.email().endsWith("@acme.com")) {
            throw new IllegalArgumentException("Invalid employee");
        }

        if (userRepository.findByEmail(requestDTO.email().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        } else {

            if (requestDTO.password().length() < 12) {
                throw new IllegalArgumentException("Password must be at least 12 characters long");

            } else if (badPasswords.contains(requestDTO.password())) {
                throw new IllegalArgumentException("The password is in the hacker's database!");

            }

            Role admin = roleRepository.findByName(RoleName.ROLE_ADMINISTRATOR);
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER);

            if (admin == null || userRole == null) {
                throw new RuntimeException("Roles are not set up properly.");
            }

            User user = User.builder()
                    .name(requestDTO.name())
                    .lastName(requestDTO.lastName())
                    .email(requestDTO.email().toLowerCase())
                    .password(passwordEncoder.encode(requestDTO.password()))
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

                return new ResponseDTO(savedUser.getId(),
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

                return new ResponseDTO(savedUser.getId(),
                        savedUser.getName(),
                        savedUser.getLastName(),
                        savedUser.getEmail().toLowerCase());
            }
        }
    }

    public ResponseDTO getApiPayment(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

        return new ResponseDTO(user.getId(),
                user.getName(),
                user.getLastName(),
                user.getEmail()
                        .toLowerCase());
    }

    public ChangePasswordResponseDTO changePswd(Principal principal, ChangePasswordRequestDTO passwordRequestDTO) {
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

            return new ChangePasswordResponseDTO(save.getEmail(),
                    "The password has been updated successfully");
        }
    }

    public List<UserGetResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(user ->
                        new UserGetResponseDTO(user.getId(),
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

    public DeleteUserEmailResponseDTO deleteUser(Principal principal, String email) {
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

        return new DeleteUserEmailResponseDTO(deletedUser.getEmail(),
                "Deleted successfully!");
    }

    public UserGetResponseDTO changeRole(UserPutRequestDTO userPutRequestDTO, Principal principal) {
        User user = userRepository.findByEmail(userPutRequestDTO.user())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Role targetRole = roleRepository.findByName(RoleName.valueOf(userPutRequestDTO.role().toUpperCase()));


        if (targetRole == null) {
            throw new IllegalArgumentException("Invalid role");
        }

        Set<Role> roles = new HashSet<>(user.getRoles());

        if (userPutRequestDTO.operation().equals("GRANT")) {

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

        if (userPutRequestDTO.operation().equals("REMOVE")) {

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

        return new UserGetResponseDTO(user.getId(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                roles.stream().map(role -> role.getName()
                                .name())
                        .sorted()
                        .toList());
    }

    public UserAdminAccessResponseDTO userAdminAccessUserDTO(
            UserAdminAccessRequestUserDTO userAdminAccessRequestUserDTO,
            Principal principal) {

        User user = userRepository.findByEmail(userAdminAccessRequestUserDTO.user().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleName.ROLE_ADMINISTRATOR))) {
            throw new IllegalArgumentException("Can't lock the ADMINISTRATOR!");
        }

        if (userAdminAccessRequestUserDTO.operation().equals(LOCK)) {
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

        } else if (userAdminAccessRequestUserDTO.operation().equals(UNLOCK)) {
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
        return new UserAdminAccessResponseDTO(
                "User " + user.getEmail() + " " + userAdminAccessRequestUserDTO.operation().name() + "!"
        );
    }
}