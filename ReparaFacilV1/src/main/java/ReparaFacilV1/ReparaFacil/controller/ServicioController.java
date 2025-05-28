package main.java.ReparaFacilV1.ReparaFacil.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ReparaFacilV1.ReparaFacil.model.Servicio;
import java.ReparaFacilV1.ReparaFacil.service.ServicioService;




@RestController
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @GetMapping
    public ResponseEntity<List<Servicio>> Listar(){
        List<Servicio> servicios = servicioService.fetchALL();
        
        if (servicios.isEmpty()) {
            // error 404 not found
            return ResponseEntity.noContent().build();
        }

        // status 200 ok
        return ResponseEntity.ok(servicios);
    }
}

@GetMapping()
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    public ResponseEntity<List<Servicio>> buscar(){
        List<Servicio> servicios = servicioService.fetchALL();
        
        if (servicios.isEmpty()) {
            // error 404 not found
            return ResponseEntity.noContent().build();
        }

        // status 200 ok
        return ResponseEntity.ok(servicios);
    }
}

