package com.example.myspringproject.repository;

import com.example.myspringproject.entity.Role;
import com.example.myspringproject.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RoleName name);
}
