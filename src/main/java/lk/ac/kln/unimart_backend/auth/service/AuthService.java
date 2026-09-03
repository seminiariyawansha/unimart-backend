package lk.ac.kln.unimart_backend.auth.service;

import lk.ac.kln.unimart_backend.auth.dto.*;
import lk.ac.kln.unimart_backend.common.exception.ConflictException;
import lk.ac.kln.unimart_backend.common.exception.UnauthorizedException;
import lk.ac.kln.unimart_backend.user.entity.User;
import lk.ac.kln.unimart_backend.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long accessMinutes;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder,
                       @org.springframework.beans.factory.annotation.Value("${app.security.access-minutes:15}") long accessMinutes) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.accessMinutes = accessMinutes;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (users.findByUniversityEmail(request.universityEmail()).isPresent()) {
            throw new ConflictException("Email already registered");
        }
        User user = new User();
        user.setUniversityEmail(request.universityEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole("USER");
        user.setEmailVerified(false);
        users.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = users.findByUniversityEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Instant now = Instant.now();
        Instant expiry = now.plus(accessMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("unimart-backend")
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(user.getUniversityEmail())
                .claim("role", user.getRole())
                .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", accessMinutes * 60);
    }
}