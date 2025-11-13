package com.example.todo.auth.controller;

// TodoController.java
import com.example.todo.auth.dto.TodoRequest;
import com.example.todo.auth.entity.Todo;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.UserRepository;
import com.example.todo.auth.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<Todo>> getUserTodos(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Todo> todos = todoService.getUserTodos(user);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Todo>> getCompletedTodos(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Todo> todos = todoService.getUserTodosByCompletion(user, true);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Todo>> getPendingTodos(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Todo> todos = todoService.getUserTodosByCompletion(user, false);
        return ResponseEntity.ok(todos);
    }

    @PostMapping
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody TodoRequest request,
                                           Authentication authentication) {
        User user = getCurrentUser(authentication);
        Todo todo = todoService.createTodo(request, user);
        return ResponseEntity.ok(todo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id,
                                           @Valid @RequestBody TodoRequest request,
                                           Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Todo> updatedTodo = todoService.updateTodo(id, request, user);
        return updatedTodo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Todo> toggleTodoCompletion(@PathVariable Long id,
                                                     Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Todo> updatedTodo = todoService.toggleTodoCompletion(id, user);
        return updatedTodo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        boolean deleted = todoService.deleteTodo(id, user);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
