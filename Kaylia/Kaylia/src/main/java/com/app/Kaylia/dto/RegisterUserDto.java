package com.app.Kaylia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserDto {

    @JsonProperty("fName")
    private String fName;
    @JsonProperty("lName")
    private String lName;
    private String email;
    private String password;
    private String role;

}
