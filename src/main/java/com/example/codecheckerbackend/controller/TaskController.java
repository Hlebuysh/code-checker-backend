package com.example.codecheckerbackend.controller;

import com.example.codecheckerbackend.dto.CodeSubmissionDTO;
import com.example.codecheckerbackend.dto.SubmissionResultDTO;
import com.example.codecheckerbackend.dto.TaskDTO;
import com.example.codecheckerbackend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmissionResultDTO> submitCode(
            @PathVariable Long id,
            @Valid @RequestBody CodeSubmissionDTO submission) {
        return ResponseEntity.ok(taskService.submitCode(id, submission.getCode()));
    }
}