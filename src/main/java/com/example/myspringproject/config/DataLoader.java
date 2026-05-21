package com.example.myspringproject.config;

import com.example.myspringproject.entity.Role;
import com.example.myspringproject.entity.RoleName;
import com.example.myspringproject.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataLoader {
    private final RoleRepository repository;

    @Autowired
    public DataLoader(RoleRepository repository) {
        this.repository = repository;
        createRoles();
    }

    private void createRoles() {
        try {

            createRoleIfNotExists(RoleName.ROLE_USER);
            createRoleIfNotExists(RoleName.ROLE_ACCOUNTANT);
            createRoleIfNotExists(RoleName.ROLE_ADMINISTRATOR);
            createRoleIfNotExists(RoleName.ROLE_AUDITOR);

        } catch (Exception e) {
            log.error("Error while creating roles: {}", e.getMessage());
        }
    }

    private void createRoleIfNotExists(RoleName roleName) {
        if (repository.findByName(roleName) == null) {
            repository.save(new Role(roleName));
        }
    }
}
