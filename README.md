# CS2024-2Grupo01

AUTORES: 
- Abel Luciano Aragon Alvaro
- Carlos Mijail Mamani Anccasi 

# MentoriaApp

## Enlace del Repositorio
[GitHub Repository](https://github.com/eluqm/CS2024-2Grupo01)

## Fase: Análisis

### Requisitos y Funcionalidades Importantes

- **Tipos de Usuarios y Permisos:**
  - **Psicóloga:** Generar reportes y gestionar la data entrante.
  - **Coordinador:** Supervisar a los mentores mediante la observación de reportes.
  - **Mentor:** Registrar asistencia y adjuntar fotos y observaciones por cada sesión.
  - **Mentoreados:** Consultar horarios y notificaciones de mentoría.

- **Gestión de Cuentas:**
  - El sistema generará cuentas automáticamente tras validar y aceptar la carga de coordinadores, mentores y mentoreados.
  - Las credenciales serán: DNI y contraseña numérica (1 al 6), compartidas por email.

- **Preguntas de análisis:**
  - "¿Podríamos usar solo DNI para los mentoreados?"

- **Registro y Notificaciones:**
  - Registro automático de la hora y fecha de asistencia.
  - Los mentores deberán adjuntar fotografías y observaciones por cada sesión.
  - Emisión de eventos personalizados por el área de psicología, con inscripción opcional para mentoreados y mentores.
  - Notificaciones de mentores a mentoreados (texto solo) en un tablón/grupo de WhatsApp público.

- **Reportes y Estadísticas:**
  - Reportes automáticos de calificaciones de mentoreados, basados en asistencia y evaluación del mentor.
  - Reportes descargables en formato CSV para análisis externo.
  - Informe receptación de mentoreados y coordinadores, con opción de adjuntar documentos en la nube.

- **Configuración de Horarios:**
  - Los mentores podrán configurar su horario (1 sesión de 45 min por semana), visible para los mentoreados.

- **Carga de Datos:**
  - **Carga Masiva:**
    - Mentoreados: Carga semi-automática mediante CSVs por semestre.
    - Mentores y Coordinadores: Carga automática o manual (mediante email o formulario).

- **Encuestas:**
  - El sistema soporta encuestas de rendimiento para mentores y coordinadores.

## Fase: Casos de Uso

(Se detallarán en la documentación específica de casos de uso.)

### Login
- Actores: Todos.
- Idea: La primera vez que usuario inicie sesión puede ingresar mediante su DNI, el sistema valida si el DNI pertenece a la organización y si es asi le permite poner una contraseña de respaldo.

### Carga Masiva:
- Actores: Psicología.
- 
- DATA-USUARIOS: Se aplicara para el llenado de datos de los coodinadores, mentores y mentoriados. Se manejra por carrera, o sea en caso del CSV un CSV por carrera. O un SOLO CSV bien estructurado.
  - CSV: Se podra subir la data desde un CSV que previamente se proporcionara como platilla para que pueda ser leido.
  - Manual: Cuadros de textos para insertar los datos de una entidad en especifico.
-> Pregunta a la Psicologa como es que recibe los datos de los estudiantes cada inicio de semestre(formato, cantidas de archivos).

- DATA-AULAS: Una vez que la universidad le otorgue el permiso de ciertas **aulas** en ciertos **horarios** este podra ser subido mediante un CSV como un pull de aulas.
- La inserción podria ser manual, podria agregarse más o modificarse los ya existentes horarios.
- Se puede publicar para el uso de los mentoriados.

### Creación y Reparto de grupos
- Actores: Coordinadores.
- Primero se selecciona a el mentor o a los mentores(dupla).
- Se puede poner un cantidad de mentoriados para asignar a este grupo, como por ejemplo 20.
- // Te deria dejar editar(quitar o agregar más)
- Listo.


### Modificar grupos ya creados.
- Actores: Coordinadores.
- Una vez creado el grupo puede hacer correciones como.
  -  Quitar mentoriados de un grupo
  -  Cambiar de mentor a un grupo
  -  Agregar mentoriados a un grupo (trazladar mentoriados de un grupo a otro)
- El coodinador puede modificar los grupos a cada rato o cuando se plazca o tendria que haber ciertos parametros:
  -  Tiempo máximo para que pueda editar una vez guarda o modificada por ultima vez el grupo.
  -  Solicitud a Psicología para realizar cambios.

### Gestion de Horario:
- Actores: Mentores.
- Selecciona un horario disponible. Se remite este horario a los mentoriados(notificaiones o que se publique en el tablon automaticamnte.).
- Puedes hacer cambio de horario. Solicitando permiso a Psicología o al coodinador, esperando un visto bueno.
- Tambien puedes dar una propuesta ingresando el dia, la hora y el aula. Seria enviado a Psicología.



OJO: Como controlar el reparto de aulas.

## Fase: Diseño

- **Login**
  - Pantalla de inicio para cada tipo de usuario.
  - Pantalla de registro de asistencia (de mentor a mentoreados).

- **Tareas:**
  - Diseñar el logo de la aplicación.
  - Crear maquetas de:
    - Pantalla de inicio de cada usuario.
    - Pantalla de registro de asistencia (de mentor a mentoreados).
