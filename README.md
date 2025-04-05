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
![image](https://github.com/user-attachments/assets/9e14a481-2511-4d3c-a95d-f560934c2aeb)
Entrando como administrador
![image](https://github.com/user-attachments/assets/91dba105-1539-498c-9201-e218fb988eee)
Gestión de Usuarios como Administrador
![image](https://github.com/user-attachments/assets/4d07d49a-2d2b-4794-9ed8-1f0e87f0ffd7)
Entrando como usuario normal
![image](https://github.com/user-attachments/assets/42d468c4-064f-45e7-87b0-8c2a89b89cde)
Modo oscuro en editar perfil
![image](https://github.com/user-attachments/assets/b78affab-d493-405e-9501-950fdedbe646)
Página de inicio en modo oscuro
![image](https://github.com/user-attachments/assets/4e852a1f-e4d5-4e32-a436-960bfacc9fbe)
Módulo de Búsqueda de artículos
![image](https://github.com/user-attachments/assets/763991b2-d3b3-4abc-a0f5-c782dbaaeedd)
Resultados de búsqueda (en este caso machine learning)
![image](https://github.com/user-attachments/assets/5cd2927a-0e3a-4db3-ac5c-cbad2ebb3f16)
![image](https://github.com/user-attachments/assets/064e6b2e-7c80-4472-93e4-d5ca1a0fa054)
Opciones de Búsqueda Avanzada
![image](https://github.com/user-attachments/assets/46849787-93af-422f-9cec-fce4cf4c996f)

Imagenes en docker
![image](https://github.com/user-attachments/assets/f8a7ad96-2286-48f3-97a4-d524662b0035)

Contenedor de docker
![image](https://github.com/user-attachments/assets/8bef88a5-5ba3-4560-9b3b-7ed32c09fb18)

Se asegura la conexión en la base de datos usando docker
![image](https://github.com/user-attachments/assets/e9bdfc09-c8ff-406e-ae66-1f1bb0de8f6d)


