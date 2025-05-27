package main.java.ReparaFacilV1.ReparaFacil.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import main.java.ReparaFacilV1.ReparaFacil.respository.ServicioRepository;



@Service
@Transactional
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

}
