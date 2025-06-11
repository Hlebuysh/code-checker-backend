package com.example.codecheckerbackend.repository;

import com.example.codecheckerbackend.model.Submission;
import com.example.codecheckerbackend.model.Task;
import com.example.codecheckerbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserAndTask(User user, Task task);
    Optional<Submission> findTopByUserAndTaskOrderBySubmittedAtDesc(User user, Task task);
}