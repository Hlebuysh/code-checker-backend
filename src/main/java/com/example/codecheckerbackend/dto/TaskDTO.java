package com.example.codecheckerbackend.dto;

import lombok.Data;

@Data
public class TaskDTO {
    public void setId(Long id) {
        this.id = id;
    }

    private Long id;

    public void setTitle(String title) {
        this.title = title;
    }

    private String title;

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    private String difficulty;

    public void setInitialCode(String initialCode) {
        this.initialCode = initialCode;
    }

    private String initialCode;

    public void setLastAttemptSuccess(Boolean lastAttemptSuccess) {
        this.lastAttemptSuccess = lastAttemptSuccess;
    }

    private Boolean lastAttemptSuccess;
}
