package com.example.codecheckerbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
public class Task {
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String getTitle() {
        return title;
    }

    @Column(nullable = false)
    private String title;

    public String getDescription() {
        return description;
    }

    @Column(columnDefinition = "TEXT")
    private String description;

    public String getInitialCode() {
        return initialCode;
    }

    @Column(columnDefinition = "TEXT")
    private String initialCode;

    public String getTestScenario() {
        return testScenario;
    }

    @Column(columnDefinition = "TEXT")
    private String testScenario; // Cucumber scenario

    public String getDifficulty() {
        return difficulty;
    }

    private String difficulty;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<Submission> submissions = new ArrayList<>();
}