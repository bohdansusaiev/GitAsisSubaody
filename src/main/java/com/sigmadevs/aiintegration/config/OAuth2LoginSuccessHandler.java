package com.sigmadevs.aiintegration.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sigmadevs.aiintegration.entity.Role;
import com.sigmadevs.aiintegration.entity.User;
//import com.sigmadevs.aiintegration.exception.Oauth2Exception;
import com.sigmadevs.aiintegration.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtUtil jwtUtils;
    @Value("${spring.security.domain}")
    private String domain;
    @Value("${spring.profiles.active}")
    private String profile;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        String provider = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
        if ("github".equals(provider) ) {
            String username;
            DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = principal.getAttributes();
            Map<String, Object> modifiableMap = new HashMap<>(attributes);
            String email = attributes.getOrDefault("email", "") != null ? attributes.getOrDefault("email", "").toString() : getPrimaryEmailForGitHub(authentication);
            modifiableMap.put("email", email);
            String avatarUrl;
            String name = attributes.getOrDefault("name", attributes.getOrDefault("given_name",email.split("@")[0])).toString();
            if ("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
               avatarUrl = attributes.getOrDefault("avatar_url", "").toString();
                username = attributes.getOrDefault("login", "").toString();
            } else {
                avatarUrl = "";
                username = "";
            }
//            else {
//                throw new Oauth2Exception("Oauth Exception");
//            }
            log.debug("HELLO OAUTH: {} : {} : {}", email, name, username);

            AtomicReference<User> main = new AtomicReference<>();
            userService.getUserByEmail(email)
                    .ifPresentOrElse(user -> {
                        main.set(user);
                        DefaultOAuth2User oauthUser = new DefaultOAuth2User(
                                Collections.singleton(user.getRole()),
                                modifiableMap,
                                "email"
                        );
                        Authentication securityAuth = new OAuth2AuthenticationToken(
                                oauthUser,
                                oauthUser.getAuthorities(),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
                        );
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    }, () -> {
                        User newUser = new User();
                        newUser.setRole(Role.USER);
                        newUser.setUsername(username);
                        newUser.setImage(avatarUrl);
                        newUser.setEmail(email);
                        newUser.setLoginToken(getAccessToken(authentication));
                        User saved = userService.registerUser(newUser);
                        main.set(saved);
                        DefaultOAuth2User oauthUser = new DefaultOAuth2User(
                                Collections.singleton(newUser.getRole()),
                                modifiableMap,
                                "email"
                        );
                        Authentication securityAuth = new OAuth2AuthenticationToken(
                                oauthUser,
                                oauthUser.getAuthorities(),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
                        );
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    });

            User user = main.get();
            String domainCurrent;
            if(profile.equals("dev")){
                domainCurrent = "localhost";
            }else {

                domainCurrent = domain.replaceFirst("^(https?://)", ".");
            }
            ResponseCookie responseCookie = ResponseCookie.from("accessToken", jwtUtils.generateToken(user)).
                    httpOnly(true).
                    maxAge(24_192_000).
                    sameSite("None").
                    path("/").
                    secure(true).
                    domain(domainCurrent).
                    build();
            Objects.requireNonNull(response).addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
            this.setAlwaysUseDefaultTargetUrl(true);
            this.setDefaultTargetUrl(domain + "/profile");
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    private String getPrimaryEmailForGitHub(Authentication authentication) throws JsonProcessingException {
        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient(
                        ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId(),
                        authentication.getName());
        String accessToken = client.getAccessToken().getTokenValue();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "https://api.github.com/user/emails";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String json = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> emails = objectMapper.readValue(json, new TypeReference<>() {
        });
        return emails.stream()
                .filter(email -> Boolean.TRUE.equals(email.get("primary")))
                .map(email -> (String) email.get("email"))
                .findFirst()
                .orElseThrow();
    }
    public String getAccessToken(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthToken) {
            String clientRegistrationId = oAuth2AuthToken.getAuthorizedClientRegistrationId();

            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    clientRegistrationId, oAuth2AuthToken.getName());

            if (authorizedClient != null) {
                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                return accessToken != null ? accessToken.getTokenValue() : null;
            }
        }
        return null;
    }
}