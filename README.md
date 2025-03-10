## InfromAPI - Spring Boot

Este proyecto es una aplicación web desarrollada con Spring Boot que implementa un sistema de gestión de usuarios con autenticación, registro y control de acceso basado en roles. La aplicación permite a los usuarios registrarse, iniciar sesión, editar su perfil y, en el caso de administradores, gestionar a todos los usuarios del sistema.

## Características principales

- Autenticación y autorización con Spring Security
- Registro de nuevos usuarios
- Perfiles de usuario y administrador
  **Por defecto se tiene de administrador
     usuario: Antonio
     contraseña: 12345**
- Gestión completa de usuarios (CRUD) para administradores
- Interfaz de usuario responsive con Bootstrap y Thymeleaf
- Persistencia de datos con PostgreSQL

## Instalación y ejecución LOCAL
1. Clonar el repositorio

2. Configurar la base de datos
Asegúrate de tener PostgreSQL ejecutándose y crea una base de datos llamada basesita:
Para crear la base, usar el schema.sql que está en este repositorio

4. Compilar y ejecutar la aplicación con mvn spring-boot:run
La aplicación estará disponible en http://localhost:5173


## Despliegue con Docker

La forma más sencilla de ejecutar la aplicación es utilizando Docker Compose, que configura automáticamente tanto la aplicación como la base de datos PostgreSQL.

### Requisitos previos

Solo necesitas tener instalado:
- Docker
- Docker Compose
- Git

### Pasos para ejecutar

1. Clonar el repositorio:

```bash
git clone https://github.com/tu-usuario/tarea3.git
cd tarea3
```

2. Iniciar la aplicación con Docker Compose:

```bash
docker-compose up --build
```

Este comando:
- Construirá la imagen Docker de la aplicación
- Iniciará un contenedor PostgreSQL con la configuración necesaria
- Conectará ambos contenedores
- Expondrá la aplicación en http://localhost:5173

3. Acceder a la aplicación:

Una vez que los contenedores estén en funcionamiento, abre tu navegador y visita:
http://localhost:5173

4. Para detener los contenedores:

```bash
docker-compose down
```

Si deseas eliminar también los datos persistentes:

```bash
docker-compose down -v
```

## Usuarios y roles

La aplicación tiene dos tipos de roles:

1. **ROLE_USER**: Usuarios normales que pueden:
   - Ver y editar su propio perfil
   - Cambiar su contraseña

2. **ROLE_ADMIN**: Administradores que además pueden:
   - Ver la lista de todos los usuarios
   - Editar cualquier usuario
   - Eliminar usuarios

## Capturas de pantalla
Pagina de Login
![image](https://github.com/user-attachments/assets/59c6d55c-af4d-4254-aa5b-dde49ae768a7)

Registrando al usuario aimep3
![image](https://github.com/user-attachments/assets/04611eb5-5aa4-4e22-87b8-24675d381194)

Iniciando sesión
![image](https://github.com/user-attachments/assets/fd294936-fbb8-4b78-ba93-a2bea44b1c7c)

![image](https://github.com/user-attachments/assets/e8b6b75f-350e-4c3a-8f51-597d9d378387)

Editando el nombre a aimep4
![image](https://github.com/user-attachments/assets/82180f23-a419-40f8-bdf3-6c68bf1ce53d)
![image](https://github.com/user-attachments/assets/bf505b5c-7d39-4bf3-a11a-0347e18baf55)

Pantalla de Administrador
![image](https://github.com/user-attachments/assets/e61e58cb-2ee2-4760-b886-087b5b7fa286)

Lista de usuarios
![image](https://github.com/user-attachments/assets/3969874e-dbd4-4c90-a9f3-2721001c8bba)

Editando aimep4 a aimep3
![image](https://github.com/user-attachments/assets/c2215b2c-ff36-4607-9ba7-ea6375432920)
![image](https://github.com/user-attachments/assets/81925aa5-1780-42f6-b638-bc097322bdee)

Eliminado aimep3
![image](https://github.com/user-attachments/assets/cfe999cf-3d14-4bf3-b6ba-bc2654c67bc2)
![image](https://github.com/user-attachments/assets/79555713-da09-42dd-af1e-af62ed09f80d)

Imagenes en docker
![image](https://github.com/user-attachments/assets/be17b799-a58b-4e0a-b472-b0530f1db5c7)

Contenedor de docker
![image](https://github.com/user-attachments/assets/ce9cabee-8e11-4dd3-8fec-31406acb73b5)

Se asegura la conexión en la base de datos usando docker
![image](https://github.com/user-attachments/assets/e9bdfc09-c8ff-406e-ae66-1f1bb0de8f6d)


