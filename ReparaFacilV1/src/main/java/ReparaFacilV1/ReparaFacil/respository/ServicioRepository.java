package main.java.ReparaFacilV1.ReparaFacil.respository;.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.respository;


import ReparaFacilV1.ReparaFacil.model.Servicio;
import ReparaFacilV1.ReparaFacil.model.Solicitud;
import main.java.ReparaFacilV1.ReparaFacil.model.Pago;
import main.java.ReparaFacilV1.ReparaFacil.model.Tecnico;
import main.java.ReparaFacilV1.ReparaFacil.model.Valoracion;

import java.util.List;

import ReparaFacilV1.ReparaFacil.model.Servicio;


@Respository
public interface ServicioRepository extends JpaRepository<Servicio, Cliente, Pago, Solicitud, Tecnico, Valoracion, Long> {

    public static List<Servicio> findALL();

    public Object findById(Long id);

    }