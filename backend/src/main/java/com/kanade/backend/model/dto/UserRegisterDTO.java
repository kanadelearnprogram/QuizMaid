package com.kanade.backend.model.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String userName;
    private String userPassword;
    private String checkUserPassword;
}
