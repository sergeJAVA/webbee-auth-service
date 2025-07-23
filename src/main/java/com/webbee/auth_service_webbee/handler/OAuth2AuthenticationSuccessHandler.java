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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
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
        Set<Role> roles = roleRepository.findByName("USER").stream().collect(Collectors.toSet());
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .username(name)
                    .email(email)
                    .password(null)
                    .roles(roles)
                    .authType(AuthType.GOOGLE)
                    .build();
            return userRepository.save(newUser);
        });

        String token = jwtService.generateJwtToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/callback")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}
