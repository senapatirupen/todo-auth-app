package com.example.todo.auth.service;

// TodoService.java
import com.example.todo.auth.dto.TodoRequest;
import com.example.todo.auth.entity.Todo;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    public List<Todo> getUserTodos(User user) {
        return todoRepository.findByUser(user);
    }

    public List<Todo> getUserTodosByCompletion(User user, boolean completed) {
        return todoRepository.findByUserAndCompleted(user, completed);
    }

    public Todo createTodo(TodoRequest request, User user) {
        Todo todo = new Todo(request.getTitle(), request.getDescription(), user);
        return todoRepository.save(todo);
    }

    public Optional<Todo> updateTodo(Long id, TodoRequest request, User user) {
        return todoRepository.findById(id)
                .map(todo -> {
                    if (!todo.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("Not authorized to update this todo");
                    }
                    todo.setTitle(request.getTitle());
                    todo.setDescription(request.getDescription());
                    todo.setUpdatedAt(LocalDateTime.now());
                    return todoRepository.save(todo);
                });
    }

    public Optional<Todo> toggleTodoCompletion(Long id, User user) {
        return todoRepository.findById(id)
                .map(todo -> {
                    if (!todo.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("Not authorized to update this todo");
                    }
                    todo.setCompleted(!todo.isCompleted());
                    todo.setUpdatedAt(LocalDateTime.now());
                    return todoRepository.save(todo);
                });
    }

    public boolean deleteTodo(Long id, User user) {
        return todoRepository.findById(id)
                .map(todo -> {
                    if (!todo.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("Not authorized to delete this todo");
                    }
                    todoRepository.delete(todo);
                    return true;
                })
                .orElse(false);
    }
}
