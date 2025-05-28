package main.java.ReparaFacilV1.ReparaFacil.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ReparaFacilV1.ReparaFacil.model.Servicio;

import java.ReparaFacilV1.ReparaFacil.service.ServicioService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;




@RestController
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @GetMapping
    public ResponseEntity<List<Servicio>> listar(){
        List<Servicio> servicios = servicioService.fetchALL();
        
        if (servicios.isEmpty()) {
            // error 404 not found
            return ResponseEntity.noContent().build();
        }

        // status 200 ok
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servicio> buscar(@PathVariable Integer id) {
   
        Servicio servicio = servicioService.fetchById(id);
        
        // status 200 ok
        return ResponseEntity.ok(servicio);
    }

    @PostMapping
    public ResponseEntity<Servicio> guardar(@RequestBody Servicio servicio) {
        Servicio servicioMuevo = servicioService.save(servicio);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioMuevo);
        // return new responseEntity<> (prductoMuevo, HttpStatus.ACcepted);
        
    }

}