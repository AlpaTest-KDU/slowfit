package com.slowfit.slowfit.domain.user.controller;

import com.slowfit.slowfit.domain.user.dto.UserLoginRequestDto;
import com.slowfit.slowfit.domain.user.dto.UserRequestDto;
import com.slowfit.slowfit.domain.user.dto.UserResponseDto;
import com.slowfit.slowfit.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signUp(@Valid @RequestBody UserRequestDto requestDto) {
        UserResponseDto responseDto = userService.signUp(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginRequestDto requestDto) {
        String token = userService.login(requestDto.getUsername(), requestDto.getPassword());
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(Collections.singletonMap("username", username));
    }
}
