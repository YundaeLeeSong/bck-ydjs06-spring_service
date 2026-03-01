package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskCreateRequest;
import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskDto createTask(TaskCreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = new Task(request.title(), request.description(), TaskStatus.PENDING, user);
        Task savedTask = taskRepository.save(task);

        return mapToDto(savedTask);
    }

    public List<TaskDto> getUserTasks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return taskRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TaskDto getTaskById(Long id, String username, boolean isAdmin) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (!isAdmin && !task.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not authorized to view this task");
        }

        return mapToDto(task);
    }

    public TaskDto updateTaskStatus(Long id, TaskStatus status, String username, boolean isAdmin) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (!isAdmin && !task.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not authorized to update this task");
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        return mapToDto(updatedTask);
    }

    public void deleteTask(Long id, String username, boolean isAdmin) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (!isAdmin && !task.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not authorized to delete this task");
        }

        taskRepository.delete(task);
    }

    private TaskDto mapToDto(Task task) {
        return new TaskDto(task.getId(), task.getTitle(), task.getDescription(), task.getStatus());
    }
}
