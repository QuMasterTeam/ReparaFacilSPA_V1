package com.reparafacilspa.reparaciones.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> testSimple() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "Endpoint simple funcionando",
            "timestamp", new java.util.Date().toString()
        ));
    }
    
    @GetMapping("/hateoas-basic")
    public ResponseEntity<Map<String, Object>> testHateoasBasic() {
        return ResponseEntity.ok(Map.of(
            "status", "OK", 
            "message", "HATEOAS básico funcionando",
            "_links", Map.of(
                "self", "http://localhost:8081/reparafacil-api/api/test/hateoas-basic"
            )
        ));
    }
}