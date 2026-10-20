package tz.ac.dit.ipttracker.service;

import tz.ac.dit.ipttracker.dto.CreateUserRequest;
import tz.ac.dit.ipttracker.dto.PageResponse;
import tz.ac.dit.ipttracker.dto.UserResponse;
import tz.ac.dit.ipttracker.entity.User;
import tz.ac.dit.ipttracker.exception.DuplicateResourceException;
import tz.ac.dit.ipttracker.exception.ResourceNotFoundException;
import tz.ac.dit.ipttracker.mapper.UserMapper;
import tz.ac.dit.ipttracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(CreateUserRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new DuplicateResourceException("A user with email " + email + " already exists");
        }
        User u = new User();
        u.setFullName(r.fullName().trim());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(r.password()));   // BCrypt, never stored in plain text
        u.setRole(r.role());
        u.setPhone(r.phone());
        return UserMapper.toResponse(users.save(u));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Pageable pageable) {
        return PageResponse.from(users.findAll(pageable).map(UserMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserMapper.toResponse(users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found")));
    }
}
