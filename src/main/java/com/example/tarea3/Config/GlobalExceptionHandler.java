package com.example.tarea3.Config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        System.err.println("Error global capturado: " + e.getMessage());
        e.printStackTrace();
        
        model.addAttribute("errorMessage", "Ha ocurrido un error: " + e.getMessage());
        return "error";
    }
    
    @ExceptionHandler(HttpClientErrorException.class)
    public String handleHttpClientErrorException(HttpClientErrorException e, Model model) {
        System.err.println("Error de API externa: " + e.getMessage());
        model.addAttribute("errorMessage", "Error en la comunicación con la API externa: " + e.getMessage());
        return "error";
    }
    
    @ExceptionHandler(ResourceAccessException.class)
    public String handleResourceAccessException(ResourceAccessException e, Model model) {
        System.err.println("Error de acceso a recurso externo: " + e.getMessage());
        model.addAttribute("errorMessage", "No se pudo acceder al servicio externo. Verifica tu conexión a internet.");
        return "error";
    }
}