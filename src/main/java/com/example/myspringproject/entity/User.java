package com.example.myspringproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name must be not null")
    @NotBlank
    private String name;

    @NotNull(message = "Last name must be not null")
    @NotBlank
    private String lastName;

    @NotNull
    @NotBlank
    @Email(message = "Invalid employee")
    @Column(unique = true)
    private String email;

    @NotNull(message = "Password must be not null")
    @NotBlank
    @Size(min = 12, message = "Password length must be 12 chars minimum!")
    private String password;

    @OneToMany(mappedBy = "user")
    private List<Payment> payments;

    @Column(nullable = false)
    private boolean isLocked = false;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns =
    @JoinColumn(name = "user_id"), inverseJoinColumns =
    @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @Column
    private Integer failedAttempts;
}
