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

### 2. Cuentas de Prueba para el Login (Frontend)
Para probar que el mapa y las vistas funcionan sin tener que registrar un usuario a mano en la base de datos, se crean unas **cuentas de prueba automáticamente** al iniciar la aplicación.

Cuando arranquéis la aplicación (`mvn spring-boot:run`) y vayáis a `http://localhost:8080/login`, podéis usar estas credenciales para probar los diferentes roles:

**Para entrar como Deportista:**
* **Correo electrónico:** `hola@test.com`
* **Contraseña:** `123456`

**Para entrar como Organizador:**
* **Correo electrónico:** `admin@test.com`
* **Contraseña:** `123456`

---
**Nota para el equipo:** En la raíz se encuentra el archivo `seed_data_backup.sql`. Solo hay que copiar su contenido de arriba a abajo en MySQL para tener el mapa lleno y las notificaciones listas para testear.

⚠️ **Importante:** Al actualizar `integration`, el archivo `application.properties` puede ser sobrescrito por la configuración de otro compañero. Si la app no arranca, revisa siempre que tu contraseña de MySQL local sea la correcta.
