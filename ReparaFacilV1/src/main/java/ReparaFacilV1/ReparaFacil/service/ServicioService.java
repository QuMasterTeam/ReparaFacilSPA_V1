ppackage main.java.ReparaFacilV1.ReparaFacil.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;
import org.springframework.stereotype.Service;

import ReparaFacilV1.ReparaFacil.model.*;
import jakarta.transaction.Transactional;
import main.java.ReparaFacilV1.ReparaFacil.respository.ServicioRepository;



@Service
@Transactional
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    public List<Servicio> fetchALL() {

        return ServicioRepository.findALL();

    }

    public Servicio fetchById(Long id){

        return servicioRepository.findById(id).get();
    }

    public Servicio save(Servicio servicio){
        return servicioRepository.save(servicio);
    }

    public void delete(Long id){
        servicioRepository.deleteById(id);
    }
}
