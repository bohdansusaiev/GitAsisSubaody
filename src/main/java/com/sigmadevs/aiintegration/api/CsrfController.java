package com.sigmadevs.aiintegration.api;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @GetMapping("/csrf")
    public CsrfToken generateToken(CsrfToken csrfToken) {
        return csrfToken;
    }
}
