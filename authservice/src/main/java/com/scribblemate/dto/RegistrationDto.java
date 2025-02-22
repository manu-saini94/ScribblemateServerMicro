package com.scribblemate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDto {
    private String email;

    private String password;

    private String fullName;

    private String profilePicture;

    @Override
    public String toString() {
        return "RegistrationDto{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", fullName='" + fullName + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }
}
