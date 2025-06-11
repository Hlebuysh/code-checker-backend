package com.example.codecheckerbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public void setTask(Task task) {
        this.task = task;
    }

    @ManyToOne
    @JoinColumn(name = "task_id")
    private com.example.codecheckerbackend.model.Task task;

    public void setCode(String code) {
        this.code = code;
    }

    @Column(columnDefinition = "TEXT")
    private String code;

    public boolean isSyntaxValid() {
        return syntaxValid;
    }

    public void setSyntaxValid(boolean syntaxValid) {
        this.syntaxValid = syntaxValid;
    }

    private boolean syntaxValid;

    public boolean isSemanticValid() {
        return semanticValid;
    }

    public void setSemanticValid(boolean semanticValid) {
        this.semanticValid = semanticValid;
    }

    private boolean semanticValid;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime submittedAt;

    public List<CodeError> getErrors() {
        return errors;
    }

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CodeError> errors = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        submittedAt = LocalDateTime.now();
    }
}