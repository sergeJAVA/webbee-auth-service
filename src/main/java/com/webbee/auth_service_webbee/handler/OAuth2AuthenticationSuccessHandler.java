package com.webbee.auth_service_webbee.handler;

import com.webbee.auth_service_webbee.model.AuthType;
import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.repository.RoleRepository;
import com.webbee.auth_service_webbee.repository.UserRepository;
import com.webbee.auth_service_webbee.service.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new IllegalStateException("Google did not return an email.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);

        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();

            if (user.getAuthType().equals(AuthType.LOCAL)) {
                log.warn("User with email <<{}>> tried to login with Google, but is registered locally", email);
                throw new OAuth2AuthenticationException("User is registered in another way.");
            }

        } else {
            log.info("Registering new user with email <<{}>> via Google", email);
            Set<Role> roles = roleRepository.findByName("USER").stream().collect(Collectors.toSet());
            user = User.builder()
                    .username(name)
                    .email(email)
                    .password(null)
                    .roles(roles)
                    .authType(AuthType.GOOGLE)
                    .build();
            userRepository.save(user);
        }

        String token = jwtService.generateJwtToken(user);
        String targetUrl = "http://localhost:8081/oauth2/callback?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}
