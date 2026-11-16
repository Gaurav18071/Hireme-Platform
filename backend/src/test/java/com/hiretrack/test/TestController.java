package com.hiretrack.test;

import com.hiretrack.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {
    
    @PostMapping("/validate")
    public TestRequest validateRequest(@Valid @RequestBody TestRequest request) {
        return request;
    }
    
    @GetMapping("/not-found")
    public void triggerNotFound() {
        throw new ResourceNotFoundException("Test resource not found");
    }
    
    public record TestRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        
        @Email(message = "Email must be valid")
        String email
    ) {}
}
