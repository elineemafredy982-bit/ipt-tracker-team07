package tz.ac.dit.ipttracker.mapper;

import tz.ac.dit.ipttracker.dto.UserResponse;
import tz.ac.dit.ipttracker.entity.User;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getPhone(),
                u.isActive(), u.getCreatedAt());
    }
}
