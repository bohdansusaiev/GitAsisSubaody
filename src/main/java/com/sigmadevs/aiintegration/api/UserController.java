package com.sigmadevs.aiintegration.api;

import com.sigmadevs.aiintegration.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @PostMapping("/setMainToken")
    public ResponseEntity<String> setMainToken(@RequestBody String token, Principal principal) {
        log.info("Received request to set main token for user: {}", principal.getName());
        userService.setMainToken(token, principal.getName());
        return ResponseEntity.ok("Token set successfully");
    }
}
