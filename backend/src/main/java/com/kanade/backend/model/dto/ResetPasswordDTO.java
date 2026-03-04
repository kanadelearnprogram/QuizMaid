package com.kanade.backend.model.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String password;
    private String email;
    private String code;
}
