package com.example.codecheckerbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "code_errors")
@Data
@NoArgsConstructor
@Getter
public class CodeError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private Submission submission;

    private int line;

    @Column(name = "column_number")  // Указываем имя колонки в БД
    private int column;

    private String message;
    private String type; // SYNTAX, SEMANTIC

    public CodeError(int line, int column, String message, String type) {
        this.line = line;
        this.column = column;
        this.message = message;
        this.type = type;
    }
}