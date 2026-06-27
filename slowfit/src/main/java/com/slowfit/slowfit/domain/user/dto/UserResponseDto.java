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
public class UserResponseDto {

    private Long id;
    private String username;
    private String name;
    private Integer age;
    private String gender;
    private String email;
    private Role role;
}
