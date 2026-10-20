package tz.ac.dit.ipttracker.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
