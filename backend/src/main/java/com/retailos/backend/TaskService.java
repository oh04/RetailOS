package com.retailos.backend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository){
        this.repository = repository;
    }

    private TaskResponse toTaskResponse(TaskEntity entity){
        return new TaskResponse(entity.getId(), entity.getTitle(), entity.getStatus(), entity.getCreatedAt(), entity.getDueDate());
    }

    public List<TaskResponse> getTasks(){
        List<TaskEntity> list = repository.findAllByOrderByIdAsc();
        ArrayList<TaskResponse> array = new ArrayList<>();
        for(TaskEntity task : list){
            array.add(toTaskResponse(task));
        }
        return array;
    }

    public Optional<TaskResponse> findTaskById(long id){
        Optional<TaskEntity> task = repository.findById(id);
        if(task.isEmpty()){
            return Optional.empty();
        }else{
            return Optional.of(toTaskResponse(task.get()));
        }
    }

    public TaskResponse createTask(String title, LocalDate dueDate){
        TaskEntity task = new TaskEntity(title.strip());
        task.setDueDate(dueDate);
        TaskEntity saved = repository.save(task);
        return toTaskResponse(saved);
    }

    public List<TaskResponse> findTasksByStatus(TaskStatus status){
        List<TaskEntity> list = repository.findByStatusOrderByIdAsc(status);
        ArrayList<TaskResponse> array = new ArrayList<>();
        for(TaskEntity task : list){
            array.add(toTaskResponse(task));
        }
        return array;
    }

    public List<TaskResponse> findOverdueTasks(LocalDate before){
        List<TaskEntity> list = repository.findByDueDateBeforeAndStatusNotOrderByDueDateAscIdAsc(before, TaskStatus.DONE);
        ArrayList<TaskResponse> array = new ArrayList<>();
        for(TaskEntity task : list){
            array.add(toTaskResponse(task));
        }
        return array;
    }

    @Transactional
    public Optional<TaskResponse> updateStatus(long id, TaskStatus status){
        Optional<TaskEntity> result = repository.findById(id);
        if(result.isEmpty()){
            return Optional.empty();
        }
        TaskEntity task = result.get();
        task.setStatus(status);
        TaskEntity saved = repository.save(task);
        return Optional.of(toTaskResponse(saved));
    }
}
