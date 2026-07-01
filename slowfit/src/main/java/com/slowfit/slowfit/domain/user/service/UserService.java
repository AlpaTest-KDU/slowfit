package com.slowfit.slowfit.domain.user.service;

import com.slowfit.slowfit.domain.user.dto.LoginResponseDto;
import com.slowfit.slowfit.domain.user.dto.UserRequestDto;
import com.slowfit.slowfit.domain.user.dto.UserResponseDto;
import com.slowfit.slowfit.domain.user.entitiy.Role;
import com.slowfit.slowfit.domain.user.entitiy.User;
import com.slowfit.slowfit.domain.user.repository.UserRepository;
import com.slowfit.slowfit.global.config.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UserResponseDto signUp(UserRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        User user = User.builder()
            .username(requestDto.getUsername())
            .password(passwordEncoder.encode(requestDto.getPassword()))
            .name(requestDto.getName())
            .age(requestDto.getAge())
            .gender(requestDto.getGender())
            .email(requestDto.getEmail())
            .role(Role.USER)
            .build();

        User savedUser = userRepository.save(user);

        return UserResponseDto.builder()
            .id(savedUser.getId())
            .username(savedUser.getUsername())
            .name(savedUser.getName())
            .age(savedUser.getAge())
            .gender(savedUser.getGender())
            .email(savedUser.getEmail())
            .role(savedUser.getRole())
            .build();
    }

    public LoginResponseDto login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        String token = jwtUtil.generateToken(username);
        return LoginResponseDto.builder()
            .token(token)
            .username(user.getUsername())
            .role(user.getRole())
            .build();
    }
}
