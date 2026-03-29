# UrbanActive

##  Stack Tecnológico 
* **Backend:** Java + Spring Boot
* **Frontend:** HTML5 + Thymeleaf
* **Base de Datos:** MySQL
* **Mapas:** Leaflet.js
* **API Externa:** Open-Meteo

##  Equipo
* **Product Owner:** Gonzalo Moyano Fernández
* **Scrum Master:** Simón Ruibal Nogueroles
* **Desarrollo:** Sergio Guillén Ruiz, Javier Sánchez Gil, Rubén Villalba Malo

---

## Instrucciones de Arranque para el Equipo

Para que la aplicación funcione correctamente, debes tener instalado en el equipo JDK Java, MySQL y Maven Apache.

### 1. Configuración de Base de Datos (MySQL)
Spring Boot intentará conectarse automáticamente a vuestro MySQL local seguir estos pasos para configurarlo:

1. Abrir el gestor de base de datos (Workbench por ejemplo) y crear una base de datos vacía llamada `urbanactive`.
   *(También se puede ejecutar el comando SQL: `CREATE DATABASE urbanactive;`)*
2. En el código del proyecto, abrir el archivo de configuración situado en:
   `src/main/resources/application.properties`
3. Buscar la propiedad `spring.datasource.password=1234` y **cambiar el `1234` por la contraseña que uséis cada uno en vuestro ordenador** para acceder a MySQL (si no tenéis contraseña instalada, dejadlo vacío `spring.datasource.password=`).

> *Nota: Al arrancar la aplicación, se crearán todas las tablas automáticamente.*

### 2. Cuenta de Prueba para el Login (Frontend)
Para probar que el mapa y las vistas funcionan sin tener que registrar un usuario a mano en la base de datos, se ha creado una **cuenta en la memoria del sistema**.

Cuando arranquéis la aplicación (`mvn spring-boot:run`) y vayáis a `http://localhost:8080/login`, usad estas credenciales para entrar:

* **Correo electrónico:** `hola@test.com`
* **Contraseña:** `123456`
