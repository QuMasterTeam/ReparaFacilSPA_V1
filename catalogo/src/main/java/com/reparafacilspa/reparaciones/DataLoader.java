package com.reparafacilspa.reparaciones;

import com.reparafacilspa.reparaciones.model.*;
import com.reparafacilspa.reparaciones.repository.*;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Profile("dev")
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ServicioReparacionRepository servicioReparacionRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        Faker faker = new Faker();
        Random random = new Random();

        // Verificar si ya existen datos para evitar duplicados
        if (userRepository.count() > 0) {
            System.out.println("Datos ya existen en la base de datos. Saltando la carga de datos fake.");
            return;
        }

        System.out.println("Iniciando carga de datos fake para ReparaFacilSPA...");

        // 1. Crear usuarios administradores y técnicos
        createAdminUsers(faker);
        
        // 2. Crear usuarios clientes
        createClientUsers(faker, random);
        
        // 3. Obtener técnicos para asignar servicios
        List<User> tecnicos = userRepository.findAll().stream()
                .filter(u -> u.getRol() == User.UserRole.EMPRENDEDOR)
                .toList();
        
        // 4. Crear servicios de reparación
        createServiciosReparacion(faker, random, tecnicos);

        System.out.println("Carga de datos fake completada exitosamente!");
        System.out.println("Usuarios creados: " + userRepository.count());
        System.out.println("Servicios creados: " + servicioReparacionRepository.count());
    }

    private void createAdminUsers(Faker faker) {
        // Usuario administrador por defecto
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@reparafacil.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setNombre("Administrador");
        admin.setApellido("Sistema");
        admin.setTelefono("+56912345678");
        admin.setRol(User.UserRole.ADMIN);
        admin.setActivo(true);
        admin.setFechaCreacion(new Date());
        admin.setIntentosLogin(0);
        admin.setCuentaBloqueada(false);
        userRepository.save(admin);

        // Crear algunos técnicos/emprendedores
        String[] nombresTecnicos = {"Carlos", "María", "José", "Ana", "Pedro", "Laura"};
        String[] apellidosTecnicos = {"González", "Martínez", "López", "Rodríguez", "Silva", "Torres"};
        
        for (int i = 0; i < 6; i++) {
            User tecnico = new User();
            tecnico.setUsername("tecnico" + (i + 1));
            tecnico.setEmail("tecnico" + (i + 1) + "@reparafacil.com");
            tecnico.setPassword(passwordEncoder.encode("123456"));
            tecnico.setNombre(nombresTecnicos[i]);
            tecnico.setApellido(apellidosTecnicos[i]);
            tecnico.setTelefono(faker.phoneNumber().cellPhone());
            tecnico.setRol(User.UserRole.EMPRENDEDOR);
            tecnico.setActivo(true);
            tecnico.setFechaCreacion(new Date());
            tecnico.setIntentosLogin(0);
            tecnico.setCuentaBloqueada(false);
            userRepository.save(tecnico);
        }
    }

    private void createClientUsers(Faker faker, Random random) {
        // Crear clientes
        for (int i = 0; i < 50; i++) {
            User cliente = new User();
            cliente.setUsername(faker.internet().username());
            cliente.setEmail(faker.internet().emailAddress());
            cliente.setPassword(passwordEncoder.encode("123456"));
            cliente.setNombre(faker.name().firstName());
            cliente.setApellido(faker.name().lastName());
            cliente.setTelefono("+569" + faker.number().numberBetween(10000000, 99999999));
            cliente.setRol(User.UserRole.CLIENTE);
            cliente.setActivo(true);
            
            // Fechas de creación variadas (últimos 6 meses)
            Date fechaCreacion = faker.date().past(180, TimeUnit.DAYS);
            cliente.setFechaCreacion(fechaCreacion);
            
            // Algunos con último login reciente
            if (random.nextBoolean()) {
                cliente.setUltimoLogin(faker.date().past(30, TimeUnit.DAYS));
            }
            
            cliente.setIntentosLogin(0);
            cliente.setCuentaBloqueada(false);
            userRepository.save(cliente);
        }
    }

    private void createServiciosReparacion(Faker faker, Random random, List<User> tecnicos) {
        // Tipos de dispositivos comunes en Chile
        String[] tiposDispositivos = {"Smartphone", "Laptop", "Tablet", "Computador", "Smartwatch", "Auriculares", "Consola"};
        
        // Marcas comunes
        String[] marcasSmartphones = {"Samsung", "iPhone", "Huawei", "Xiaomi", "Motorola", "LG"};
        String[] marcasLaptops = {"HP", "Dell", "Lenovo", "Asus", "Acer", "MacBook"};
        String[] marcasConsolas = {"PlayStation", "Xbox", "Nintendo"};
        
        // Problemas típicos
        String[] problemasSmartphone = {
            "Pantalla rota", "Batería no carga", "No enciende", "Cámara no funciona",
            "Audio no funciona", "Botones no responden", "Problemas de conectividad"
        };
        String[] problemasLaptop = {
            "No enciende", "Pantalla azul", "Sobrecalentamiento", "Batería no carga",
            "Teclado no funciona", "WiFi no conecta", "Ventilador hace ruido"
        };

        // Obtener lista de clientes
        List<User> clientes = userRepository.findAll().stream()
                .filter(u -> u.getRol() == User.UserRole.CLIENTE)
                .toList();

        // Crear servicios de reparación
        for (int i = 0; i < 80; i++) {
            ServicioReparacion servicio = new ServicioReparacion();
            
            // Seleccionar cliente aleatorio
            User clienteSeleccionado = clientes.get(random.nextInt(clientes.size()));
            servicio.setNombreCliente(clienteSeleccionado.getNombreCompleto());
            servicio.setTelefono(clienteSeleccionado.getTelefono());
            servicio.setEmail(clienteSeleccionado.getEmail());
            
            // Tipo de dispositivo
            String tipoDispositivo = tiposDispositivos[random.nextInt(tiposDispositivos.length)];
            servicio.setTipoDispositivo(tipoDispositivo);
            
            // Marca y modelo según tipo de dispositivo
            String marca, modelo, problema;
            switch (tipoDispositivo) {
                case "Smartphone":
                    marca = marcasSmartphones[random.nextInt(marcasSmartphones.length)];
                    modelo = marca.equals("iPhone") ? 
                        "iPhone " + (11 + random.nextInt(4)) : 
                        marca + " " + faker.commerce().productName();
                    problema = problemasSmartphone[random.nextInt(problemasSmartphone.length)];
                    break;
                case "Laptop":
                    marca = marcasLaptops[random.nextInt(marcasLaptops.length)];
                    modelo = marca + " " + faker.commerce().productName();
                    problema = problemasLaptop[random.nextInt(problemasLaptop.length)];
                    break;
                case "Consola":
                    marca = marcasConsolas[random.nextInt(marcasConsolas.length)];
                    modelo = marca.equals("PlayStation") ? "PS" + (4 + random.nextInt(2)) :
                            marca.equals("Xbox") ? "Xbox " + (random.nextBoolean() ? "One" : "Series") :
                            "Nintendo Switch";
                    problema = "No enciende o problemas de lectura";
                    break;
                default:
                    marca = faker.company().name();
                    modelo = faker.commerce().productName();
                    problema = "Falla general del dispositivo";
            }
            
            servicio.setMarca(marca);
            servicio.setModelo(modelo);
            servicio.setDescripcionProblema(problema + ". " + faker.lorem().sentence());
            
            // Fechas
            Date fechaCreacion = faker.date().past(90, TimeUnit.DAYS);
            servicio.setFechaCreacion(fechaCreacion);
            
            // Fecha agendada (normalmente después de la creación)
            Date fechaAgendada = faker.date().between(fechaCreacion, new Date());
            servicio.setFechaAgendada(fechaAgendada);
            
            // Estado aleatorio con lógica
            ServicioReparacion.EstadoReparacion[] estados = ServicioReparacion.EstadoReparacion.values();
            ServicioReparacion.EstadoReparacion estado = estados[random.nextInt(estados.length)];
            servicio.setEstado(estado);
            
            // Asignar técnico si el servicio está en proceso
            if (estado != ServicioReparacion.EstadoReparacion.AGENDADO && 
                estado != ServicioReparacion.EstadoReparacion.CANCELADO && 
                !tecnicos.isEmpty()) {
                User tecnico = tecnicos.get(random.nextInt(tecnicos.size()));
                servicio.setTecnicoAsignado(tecnico.getNombreCompleto());
            }
            
            // Fechas según estado
            if (estado == ServicioReparacion.EstadoReparacion.EN_REPARACION ||
                estado == ServicioReparacion.EstadoReparacion.COMPLETADO ||
                estado == ServicioReparacion.EstadoReparacion.ENTREGADO) {
                servicio.setFechaInicioReparacion(faker.date().between(fechaAgendada, new Date()));
            }
            
            if (estado == ServicioReparacion.EstadoReparacion.COMPLETADO ||
                estado == ServicioReparacion.EstadoReparacion.ENTREGADO) {
                Date fechaInicio = servicio.getFechaInicioReparacion() != null ? 
                    servicio.getFechaInicioReparacion() : fechaAgendada;
                servicio.setFechaFinReparacion(faker.date().between(fechaInicio, new Date()));
            }
            
            // Costos
            BigDecimal costoBase = new BigDecimal(faker.number().numberBetween(15000, 150000));
            servicio.setCostoEstimado(costoBase);
            
            if (estado == ServicioReparacion.EstadoReparacion.COMPLETADO ||
                estado == ServicioReparacion.EstadoReparacion.ENTREGADO) {
                // Costo final puede variar ±20% del estimado
                double variacion = 0.8 + (random.nextDouble() * 0.4); // 0.8 a 1.2
                BigDecimal costoFinal = costoBase.multiply(new BigDecimal(variacion));
                servicio.setCostoFinal(costoFinal);
            }
            
            // Prioridad
            ServicioReparacion.PrioridadReparacion[] prioridades = ServicioReparacion.PrioridadReparacion.values();
            servicio.setPrioridad(prioridades[random.nextInt(prioridades.length)]);
            
            // Observaciones ocasionales
            if (random.nextInt(3) == 0) {
                servicio.setObservaciones(faker.lorem().paragraph());
            }
            
            // Garantía
            servicio.setGarantiaDias(30 + random.nextInt(91)); // 30 a 120 días
            
            servicio.setActivo(true);
            
            servicioReparacionRepository.save(servicio);
        }
    }
}