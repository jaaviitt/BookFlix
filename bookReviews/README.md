# 📚 BookFlix - Plataforma de Gestión de Reseñas Literarias

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/Java-Spring%20Boot-green)
![Status](https://img.shields.io/badge/Status-MVP%20Completed-success)

## 1. Descripción del Proyecto

**BookFlix** es una aplicación web desarrollada bajo el patrón **MVC (Modelo-Vista-Controlador)** utilizando el ecosistema **Spring Boot**.

El objetivo de la aplicación es ofrecer una plataforma estilo _streaming_ (inspirada en la estética de Netflix) donde los usuarios pueden explorar un catálogo de libros, gestionar su perfil y compartir opiniones con la comunidad.

La aplicación cubre un **MVP (Producto Mínimo Viable)** completamente funcional, incluyendo gestión de usuarios, roles diferenciados (Administrador y Miembro), validaciones de datos y un sistema de reseñas interactivo.

### Funcionalidades Principales

- **Catálogo Visual:** Vista de libros con portadas, búsqueda y detalles.
- **Sistema de Reseñas:** Los usuarios pueden puntuar libros (sistema de 1 a 5 iconos de libros) y dejar comentarios.
- **Gestión de Perfil:** Los usuarios pueden editar sus datos, cambiar su contraseña y ver su historial de actividad.
- **Panel de Administración:** Acceso exclusivo para gestionar el catálogo (Crear, Editar, Listar libros).
- **Seguridad:** Login y Registro personalizados con control de acceso basado en roles (Spring Security).
- **Gestión de Errores:** Páginas personalizadas para errores (404, 403, etc.).

---

## 2. Tecnologías Utilizadas

El proyecto ha sido desarrollado utilizando las siguientes herramientas y tecnologías:

- **Backend:**
  - Java 21
  - Spring Boot 3.x (Web, Data JPA, Security, Validation)
  - Maven (Gestión de dependencias)
- **Frontend:**
  - Thymeleaf (Motor de plantillas)
  - Bootstrap 5 (Framework CSS)
  - Bootstrap Icons
  - CSS3 personalizado
- **Base de Datos:**
  - H2 Database: Base de datos embebida en memoria
- **Entorno de Desarrollo:**
  - IntelliJ IDEA

---

## 3. Requisitos de Ejecución

Para ejecutar esta aplicación en local, necesitas tener instalado:

1. **Java JDK 21** o superior.
2. **Maven** (o usar el wrapper `mvnw` incluido).
3. **MySQL Server** (corriendo en el puerto 3306).
4. **IntelliJ IDEA** (recomendado) o cualquier IDE compatible con Java.

> **Nota:** No es necesario instalar ningún servidor de base de datos (como MySQL), ya que H2 se ejecuta automáticamente al iniciar la aplicación.

---

## 4. Instalación y Puesta en Marcha

Sigue estos pasos para arrancar el proyecto:

### Paso 1: Clonar el repositorio

```bash
git clone [https://github.com/jaaviitt/bookflix.git](https://github.com/jaaviitt/bookflix.git)
cd bookflix
```

**Nota:** No es necesario instalar ningún servidor de base de datos (como MySQL), ya que H2 se ejecuta automáticamente al iniciar la aplicación.

### Paso 2: Configuración (Opcional)

El proyecto ya viene configurado para funcionar "out-of-the-box". Puedes verificar la configuración en `src/main/resources/application.properties`:

**Properties**

```
# Configuración del Servidor
server.port=9023
spring.application.name=BookReviews

# Configuración Base de Datos H2 (en memoria)
spring.datasource.url=jdbc:h2:file:./data/bookdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# "Update" crea las tablas si no existen, y si existen las actualiza.
spring.jpa.hibernate.ddl-auto=update

# Incrementamos el limite de subida de ficheros para que no rompa la subida
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Habilitar consola H2
spring.h2.console.enabled=true
```

### Paso 3: Ejecutar en IntelliJ IDEA

1. Abre el proyecto en IntelliJ.
2. Espera a que Maven descargue todas las dependencias.
3. Busca la clase principal `BookflixApplication.java`.
4. Haz clic derecho -> **Run 'BookflixApplication'** .
5. Abre tu navegador en: `http://localhost:9023`

---

## 5. Usuarios de Prueba (Roles)

Al utilizar una base de datos en memoria (H2), **los datos se reinician cada vez que cierras la aplicación** .

Para facilitar la corrección, el sistema carga automáticamente un usuario administrador y un usuario base al arrancar (definidos en `data.sql`):

| **Rol**           | **Usuario (Email)**    | **Contraseña** | **Permisos**                                              |
| ----------------- | ---------------------- | -------------- | --------------------------------------------------------- |
| **ADMINISTRADOR** | `admin@bookflix.com`   | `admin123`     | Acceso total, Panel de Control, Gestión de Libros (CRUD). |
| **USUARIO**       | `usuario@bookflix.com` | `user123`      | Ver catálogo, Escribir Reseñas, Editar Perfil propio.     |

> **Nota:** Puedes registrar nuevos usuarios libremente desde el formulario de registro (`/registro`).

---

## 6. Arquitectura y Reglas de Negocio

El proyecto sigue una estructura modular basada en capas, cumpliendo estrictamente con el patrón **Modelo-Vista-Controlador**. A continuación se detalla la organización del código fuente:

```text
src/main/java/com/trabajoFinal/bookReviews
├── config          # Configuraciones globales (Carga de datos iniciales, Beans web)
├── controller      # CAPA CONTROLADOR: Manejan las peticiones HTTP y la navegación
│   ├── AdminController.java    # Gestión del panel de administración
│   ├── AuthController.java     # Login y Registro de usuarios
│   ├── HomeController.java     # Página principal y catálogo
│   └── ...
├── entity          # CAPA MODELO: Clases POJO que representan las tablas de la BD
│   ├── Libro.java
│   ├── Resena.java
│   └── Usuario.java
├── repository      # CAPA DE DATOS: Interfaces JPA para consultas a la base de datos
├── security        # Configuración de Spring Security (Roles, encriptación, rutas protegidas)
└── service         # CAPA DE SERVICIO: Lógica de negocio (Validaciones, cálculos, llamadas a APIs)

src/main/resources
├── static          # Recursos estáticos públicos
│   ├── css         # Hojas de estilo personalizadas (auth.css, styles.css, admin.css...)
│   └── img         # Imágenes del sitio
├── templates       # CAPA VISTA: Plantillas HTML dinámicas con Thymeleaf
│   ├── admin       # Vistas protegidas para el administrador (Dashboard, Formularios)
│   ├── detalle.html
│   ├── home.html
│   ├── login.html
│   └── ...
└── application.properties # Configuración de la aplicación y base de datos
```

### Descripción de las Capas:

1. **Controller:** Reciben las interacciones del usuario, invocan a los servicios necesarios y devuelven la vista correspondiente o redirigen a otra ruta.
2. **Service:** Contiene la lógica pura de la aplicación (ej: verificar si un libro ya existe, procesar un registro, conectar con APIs externas como Google Books).
3. **Repository:** Capa de abstracción para la persistencia de datos. Extienden de `JpaRepository` para realizar operaciones CRUD sin escribir SQL manual.
4. **Entity:** Representación orientada a objetos de las tablas de la base de datos (mapeo ORM).
5. **Templates (Vista):** Archivos HTML que renderizan la información enviada por el controlador utilizando el motor de plantillas **Thymeleaf** .

### Validaciones Implementadas (Bean Validation)

Se utilizan anotaciones como `@NotNull`, `@NotBlank`, `@Size` y `@Email` para asegurar la integridad de los datos tanto en el registro de usuarios como en la creación de libros.

---

## 7. Capturas de Pantalla

![1770662268872](image/README/1770662268872.png)![1770662352128](image/README/1770662352128.png)![1770662378158](image/README/1770662378158.png)![1770662395978](image/README/1770662395978.png)![1770662414062](image/README/1770662414062.png)![1770662427850](image/README/1770662427850.png)![1770662445715](image/README/1770662445715.png)

---

**© 2026 BookFlix Project.** Desarrollado como práctica académica.
