package lk.ac.kln.unimart_backend.auth.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {}