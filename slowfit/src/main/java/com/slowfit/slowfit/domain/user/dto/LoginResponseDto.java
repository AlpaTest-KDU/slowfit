package com.slowfit.slowfit.domain.user.dto;

import com.slowfit.slowfit.domain.user.entitiy.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginResponseDto {

    private String token;
    private String username;
    private Role role;
}
