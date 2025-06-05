package com.sigmadevs.aiintegration.service;

import com.sigmadevs.aiintegration.entity.User;
import com.sigmadevs.aiintegration.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public Optional<User> getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("User with email {} already exists", user.getEmail());
            throw new IllegalArgumentException("User with this email already exists");
        }
        log.info("Registering new user with email: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", username);
                    return new UsernameNotFoundException(username);
                });
    }

    public User getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new UsernameNotFoundException(username);
                });
    }

    public void setMainToken(String token , String username) {
        log.info("Setting main token for user: {}", username);
        if (token == null || token.isBlank()) {
            log.warn("Invalid token provided for user: {}", username);
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        User user = getUserByUsername(username);
        user.setMainToken(token);
        userRepository.save(user);
        log.info("Main token set successfully for user: {}", username);
    }

}
