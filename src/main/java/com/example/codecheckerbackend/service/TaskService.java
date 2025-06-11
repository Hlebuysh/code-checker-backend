package com.example.codecheckerbackend.service;

import com.example.codecheckerbackend.dto.SubmissionResultDTO;
import com.example.codecheckerbackend.dto.TaskDTO;
import com.example.codecheckerbackend.model.*;
import com.example.codecheckerbackend.repository.SubmissionRepository;
import com.example.codecheckerbackend.repository.TaskRepository;
import com.example.codecheckerbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private CodeAnalysisService codeAnalysisService;

    @Autowired
    private CucumberTestService cucumberTestService;

    public List<TaskDTO> getAllTasks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        return taskRepository.findAll().stream()
                .map(task -> {
                    TaskDTO dto = new TaskDTO();
                    dto.setId(task.getId());
                    dto.setTitle(task.getTitle());
                    dto.setDescription(task.getDescription());
                    dto.setDifficulty(task.getDifficulty());
                    dto.setInitialCode(task.getInitialCode());

                    // Проверяем последнюю попытку пользователя
                    submissionRepository.findTopByUserAndTaskOrderBySubmittedAtDesc(user, task)
                            .ifPresent(submission -> {
                                dto.setLastAttemptSuccess(submission.isSyntaxValid() && submission.isSemanticValid());
                            });

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDifficulty(task.getDifficulty());
        dto.setInitialCode(task.getInitialCode());

        return dto;
    }

    @Transactional
    public SubmissionResultDTO submitCode(Long taskId, String code) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Task task = taskRepository.findById(taskId).orElseThrow();

        // Создаем новую попытку
        Submission submission = new Submission();
        submission.setUser(user);
        submission.setTask(task);
        submission.setCode(code);

        // Анализируем синтаксис
        CodeAnalysisService.AnalysisResult syntaxResult = codeAnalysisService.analyzeCode(code);
        submission.setSyntaxValid(syntaxResult.isSyntaxValid());

        // Добавляем синтаксические ошибки
        for (CodeError error : syntaxResult.getErrors()) {
            error.setSubmission(submission);
            submission.getErrors().add(error);
        }

        // Если синтаксис правильный, проверяем семантику
        if (syntaxResult.isSyntaxValid() && task.getTestScenario() != null) {
            // Извлекаем имя класса и метода из кода
            String className = extractClassName(code);
            String methodName = extractMethodName(task);

            if (className != null && methodName != null) {
                CucumberTestService.TestResult testResult =
                        cucumberTestService.runTests(code, className, methodName, task.getTestScenario());

                submission.setSemanticValid(testResult.isSuccess());

                // Добавляем семантические ошибки
                for (CodeError error : testResult.getErrors()) {
                    error.setSubmission(submission);
                    submission.getErrors().add(error);
                }
            }
        }

        // Сохраняем попытку
        submission = submissionRepository.save(submission);

        // Формируем результат
        SubmissionResultDTO result = new SubmissionResultDTO();
        result.setSuccess(submission.isSyntaxValid() && submission.isSemanticValid());
        result.setErrors(submission.getErrors().stream()
                .map(error -> {
                    SubmissionResultDTO.ErrorDTO errorDTO = new SubmissionResultDTO.ErrorDTO();
                    errorDTO.setLine(error.getLine());
                    errorDTO.setColumn(error.getColumn());
                    errorDTO.setMessage(error.getMessage());
                    errorDTO.setType(error.getType());
                    return errorDTO;
                })
                .collect(Collectors.toList()));

        return result;
    }

    private String extractClassName(String code) {
        // Простой парсер для извлечения имени класса
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.contains("public class")) {
                int start = line.indexOf("public class") + 13;
                int end = line.indexOf("{", start);
                if (end == -1) end = line.indexOf(" ", start);
                if (end == -1) end = line.length();
                return line.substring(start, end).trim();
            }
        }
        return "Solution"; // По умолчанию
    }

    private String extractMethodName(Task task) {
        // Извлекаем имя метода из описания задачи или используем стандартное
        // В реальном приложении это должно быть указано в задаче
        if (task.getTitle().toLowerCase().contains("sort")) {
            return "sortArray";
        } else if (task.getTitle().toLowerCase().contains("max")) {
            return "findMax";
        }
        return "solve";
    }
}