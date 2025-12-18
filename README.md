
# ReparaFacilSPA V1 🛠️

Bienvenido al repositorio de **ReparaFacilSPA**, un sistema integral para la gestión de servicios de reparación. Esta aplicación permite administrar usuarios, autenticación y el catálogo de servicios de reparación, integrando un backend robusto en Spring Boot con una interfaz web ligera.

**Asignatura:** Fullstack 1

## 👥 Integrantes del Equipo (QuMasterTeam)

* **Becker** - [MARBECK-ONE](https://github.com/MARBECK-ONE)
* **Massimo** - [THRAGG969](https://github.com/THRAGG969)
* **Vincent** - [VincentiusFarenden](https://github.com/VincentiusFarenden) (a.k.a MRBLONDIE)

---

## 🚀 Acerca del Proyecto

ReparaFacilSPA es una aplicación web diseñada para facilitar el flujo de trabajo en talleres de reparación.

### Características Principales:
* **Gestión de Usuarios:** Registro e inicio de sesión seguro (JWT).
* **Catálogo de Reparaciones:** CRUD completo para servicios de reparación.
* **Seguridad:** Implementación de Spring Security.
* **Base de Datos:** Conexión segura a Oracle Cloud Database utilizando Oracle Wallet.
* **Interfaz de Usuario:** Frontend integrado servido estáticamente (HTML/CSS/JS).

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java (JDK 17+)
* **Framework:** Spring Boot 3.x
* **Base de Datos:** Oracle Database (Cloud ATP/ADW)
* **Seguridad:** Spring Security & JWT
* **Frontend:** HTML5, CSS3, JavaScript (Vanilla)
* **Documentación API:** Swagger / OpenAPI
* **Build Tool:** Maven

---

## ⚙️ Configuración e Instalación

### Prerrequisitos
1.  Tener instalado **Java 17** o superior.
2.  Tener **Maven** instalado (o usar el wrapper `mvnw` incluido).
3.  Una instancia de base de datos Oracle.

### 1. Clonar el repositorio
```bash
git clone [https://github.com/qumasterteam/reparafacilspa_v1.git](https://github.com/qumasterteam/reparafacilspa_v1.git)
cd reparafacilspa_v1/catalogo

```

### 2. Configuración de la Base de Datos (Oracle Wallet)

El proyecto utiliza una conexión segura mediante Oracle Wallet. Asegúrate de que los archivos de la Wallet estén ubicados correctamente en:
`src/main/resources/Wallet_Repara` (o la ruta configurada en tu `application.properties`).

**Nota:** Verifica el archivo `application.properties` para confirmar la ruta de la wallet y las credenciales:

```properties
spring.datasource.url=jdbc:oracle:thin:@reparafacil_high?TNS_ADMIN=./src/main/resources/Wallet_Repara
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA

```

### 3. Ejecutar la Aplicación

Puedes iniciar el servidor utilizando el wrapper de Maven:

**En Windows:**

```cmd
mvnw.cmd spring-boot:run

```

**En Linux/Mac:**

```bash
./mvnw spring-boot:run

```

---

## 📖 Documentación de la API (Swagger)

Una vez que la aplicación esté corriendo, puedes acceder a la documentación interactiva de la API para probar los endpoints del backend:

* **URL Local:** `http://localhost:8080/swagger-ui/index.html` (o la ruta configurada en `SwaggerConfig`).

---

## 📂 Estructura del Proyecto

```text
catalogo/
├── src/
│   ├── main/
│   │   ├── java/com/reparafacilspa/reparaciones/
│   │   │   ├── config/       # Configuraciones (Seguridad, Cors, Swagger)
│   │   │   ├── controller/   # Controladores REST
│   │   │   ├── model/        # Entidades JPA
│   │   │   ├── repository/   # Repositorios de acceso a datos
│   │   │   └── service/      # Lógica de negocio
│   │   └── resources/
│   │       ├── static/       # Archivos Frontend (index.html, styles.css)
│   │       ├── Wallet_Repara/# Credenciales de Oracle Cloud
│   │       └── application.properties
└── pom.xml                   # Dependencias Maven

```

---

## 📝 Notas Adicionales

* El frontend se sirve automáticamente en la ruta raíz `/` gracias a la configuración de recursos estáticos de Spring Boot.
* Asegúrate de ejecutar los scripts SQL ubicados en `src/database/` si necesitas inicializar las tablas manualmente.

---

© 2024 ReparaFacilSPA - Desarrollado por QuMasterTeam.

```

```
