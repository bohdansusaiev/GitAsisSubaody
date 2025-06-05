package com.sigmadevs.aiintegration.repo;

import com.sigmadevs.aiintegration.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(@NotBlank String username);

    Optional<User> findByEmail(@NotBlank String email);


    boolean existsUserByUsername(@NotBlank String username);

    boolean existsUserByEmail(@NotBlank String email);

    void deleteByUsername(@NotBlank String username);

}
