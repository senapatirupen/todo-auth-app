package com.example.todo.auth.repository;

// TodoRepository.java
import com.example.todo.auth.entity.Todo;
import com.example.todo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByUser(User user);
    List<Todo> findByUserAndCompleted(User user, boolean completed);
}
