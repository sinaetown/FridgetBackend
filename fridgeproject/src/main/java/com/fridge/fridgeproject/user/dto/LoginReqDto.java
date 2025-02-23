package com.fridge.fridgeproject.user.dto;

import lombok.Data;

@Data
public class LoginReqDto {
    private String userId;
    private String userPassword;
}
