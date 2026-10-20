package tz.ac.dit.ipttracker.dto;

import tz.ac.dit.ipttracker.entity.Role;
import java.time.Instant;

/** Deliberately has no password / passwordHash field. */
public record UserResponse(Long id, String fullName, String email, Role role, String phone, boolean active,
                           Instant createdAt) {
}
