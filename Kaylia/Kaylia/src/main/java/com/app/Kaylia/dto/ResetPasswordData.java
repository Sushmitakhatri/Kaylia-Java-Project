package com.app.Kaylia.dto;

import lombok.Data;

@Data
public class ResetPasswordData {
    public String email;
    public String currentPassword;
}

