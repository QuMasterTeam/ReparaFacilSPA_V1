package main.java.ReparaFacilV1.ReparaFacil.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ReparaFacilV1.ReparaFacil.model.Servicio;

import java.util.List;

import ReparaFacilV1.ReparaFacil.model.Servicio;


@Respository
public interface ServicioRepository extends JpaRepository<Servicio, Cliente, Pago, Solicitud, Tecnico, Valoracion, Long> {

    public static List<Servicio> findALL();

    public Object findById(Long id);

    }