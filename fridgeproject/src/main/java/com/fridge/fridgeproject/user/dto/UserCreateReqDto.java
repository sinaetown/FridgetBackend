package com.fridge.fridgeproject.user.dto;

import com.fridge.fridgeproject.user.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserCreateReqDto {
    private String name;
    private String userId;
    private String userPassword;
    private Role role;
    private String vegan;
    private int meatConsumption;
    private int fishConsumption;
    private int vegeConsumption;
    private List<String> cookingMethod;
    private int spiciness;
}
