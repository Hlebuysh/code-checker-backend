package com.example.codecheckerbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubmissionResultDTO {
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setErrors(List<ErrorDTO> errors) {
        this.errors = errors;
    }

    private boolean success;
    private List<ErrorDTO> errors;

    @Data
    public static class ErrorDTO {
        private int line;
        private int column;
        private String message;
        private String type;

        public void setLine(int line) {
            this.line = line;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}