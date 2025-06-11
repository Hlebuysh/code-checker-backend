package com.example.codecheckerbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
public class CodeSubmissionDTO {
    public String getCode() {
        return code;
    }

    @NotBlank
    private String code;
}