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
![image](https://github.com/user-attachments/assets/b112a509-3574-49af-b2ce-e572066628fa)
Entrando como administrador
![image](https://github.com/user-attachments/assets/94c6988a-c309-4c6c-842c-7f82a2c3e288)
Gestión de Usuarios como Administrador
![image](https://github.com/user-attachments/assets/7ddfdc1a-33c1-4b4c-9bbd-245774e5ddfe)
Entrando como usuario normal
![image](https://github.com/user-attachments/assets/7d0014e8-485b-4132-846b-ea38a6e1e79d)
Modo oscuro en editar perfil
![image](https://github.com/user-attachments/assets/8c94733e-6d9d-4303-83d3-81014b17c095)
Página de inicio en modo oscuro
![image](https://github.com/user-attachments/assets/aca0775b-741d-4abe-9273-ca4842ef484f)
Módulo de Búsqueda de artículos
![image](https://github.com/user-attachments/assets/bdc627b9-fd18-4bb0-a980-2daa350cd621)
Resultados de búsqueda (en este caso machine learning)
![image](https://github.com/user-attachments/assets/2a5f0221-e45c-4dae-a063-d3a3213d07e9)
![image](https://github.com/user-attachments/assets/d624ba10-0711-4518-b46f-cb982619607d)
Opciones de Búsqueda Avanzada
![image](https://github.com/user-attachments/assets/94c4087a-7dd8-4d5b-ad41-3bb3840a62f5)

Imagenes en docker
![image](https://github.com/user-attachments/assets/be17b799-a58b-4e0a-b472-b0530f1db5c7)

Contenedor de docker
![image](https://github.com/user-attachments/assets/ce9cabee-8e11-4dd3-8fec-31406acb73b5)

Se asegura la conexión en la base de datos usando docker
![image](https://github.com/user-attachments/assets/e9bdfc09-c8ff-406e-ae66-1f1bb0de8f6d)


