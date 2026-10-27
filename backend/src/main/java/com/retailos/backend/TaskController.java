package com.retailos.backend;

import java.util.List;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;

@RestController
public class TaskController {

    private final TaskService service;
    
    public TaskController(TaskService service){
        this.service = service;
    }

    @GetMapping("/api/tasks")
    public List<TaskResponse> getTasks(@RequestParam(name = "status", required = false) TaskStatus status){
        if(status == null){
            return service.getTasks();
        }
        return service.findTasksByStatus(status);
    }

    @GetMapping("/api/tasks/{id}")
    public TaskResponse getTaskById(@PathVariable long id){
        Optional<TaskResponse> task = service.findTaskById(id);
        if(task.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        return task.get();
    }

    @GetMapping("/api/tasks/overdue")
    public List<TaskResponse> date(@RequestParam(name = "before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate before){
        return service.findOverdueTasks(before);
    }

    @PostMapping("/api/tasks")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request){
        TaskResponse created = service.createTask(request.title(), request.dueDate());
        return ResponseEntity.created(URI.create("/api/tasks/" + created.id())).body(created);
    }

    @PatchMapping("/api/tasks/{id}/status")
    public TaskResponse update(@PathVariable long id, @Valid @RequestBody UpdateTaskStatusRequest request){
        Optional<TaskResponse> task = service.updateStatus(id, request.status());
        if(task.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        return task.get();
    }
}
