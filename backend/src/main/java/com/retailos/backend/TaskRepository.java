package com.retailos.backend;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long>{

    List<TaskEntity> findAllByOrderByIdAsc();
    List<TaskEntity> findByStatusOrderByIdAsc(TaskStatus status);
    List<TaskEntity> findByDueDateBeforeAndStatusNotOrderByDueDateAscIdAsc(LocalDate before, TaskStatus status);
}
