package com.iconsulting.backend.features.auth.service;

import com.iconsulting.backend.common.exception.ResourceNotFoundException;
import com.iconsulting.backend.features.auth.dto.AuthResponse;
import com.iconsulting.backend.features.auth.dto.LoginRequest;
import com.iconsulting.backend.features.auth.dto.RegisterRequest;
import com.iconsulting.backend.features.user.dto.UserDto;
import com.iconsulting.backend.features.user.entity.User;
import com.iconsulting.backend.features.user.mapper.UserMapper;
import com.iconsulting.backend.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification
 * Gère le login, register, et génération de tokens JWT
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Inscription d'un nouvel utilisateur
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Créer l'utilisateur
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Hash du mot de passe
                .phone(request.getPhone())
                .role(User.UserRole.USER) // Rôle par défaut
                .active(true)
                .deleted(false)
                .emailVerified(false)
                .loginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Générer les tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Convertir en DTO et retourner la réponse
        UserDto userDto = userMapper.toDto(savedUser);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000) // Convertir ms en secondes
                .user(userDto)
                .build();
    }

    /**
     * Connexion d'un utilisateur existant
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        // Authentifier l'utilisateur avec Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Récupérer l'utilisateur depuis la base
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        // Mettre à jour lastLogin et réinitialiser loginAttempts
        user.setLastLogin(java.time.LocalDateTime.now());
        user.setLoginAttempts(0);
        userRepository.save(user);

        // Générer les tokens
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User logged in successfully: {}", request.getEmail());

        // Convertir en DTO et retourner la réponse
        UserDto userDto = userMapper.toDto(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(userDto)
                .build();
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /**
     * Rafraîchir le token d'accès avec un refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        // Extraire l'email du refresh token
        String userEmail = jwtService.extractUsername(refreshToken);

        // Charger l'utilisateur
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // Vérifier si le refresh token est valide
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Générer un nouveau access token
        String newAccessToken = jwtService.generateToken(userDetails);

        // Récupérer l'utilisateur depuis la base
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        UserDto userDto = userMapper.toDto(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Garder le même refresh token
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(userDto)
                .build();
    }

    /**
     * User logout
     * Updates lastLogout timestamp to invalidate all existing tokens
     */
    @Transactional
    public void logout(String email) {
        log.info("User logout: {}", email);

        // Retrieve user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        // Update logout timestamp
        user.setLastLogout(java.time.LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged out successfully: {}", email);
    }
}
