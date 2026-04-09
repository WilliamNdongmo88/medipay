package com.medipay.dto;

import com.medipay.enums.Role;
import lombok.Data;

@Data
public class SignupRequest {

    private String username;
    private String email;
    private String password;
    private Role role;

}