# CS2024-2Grupo01

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

- **Preguntas Abiertas:**
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

## Fase: Diseño

- **Login**
  - Pantalla de inicio para cada tipo de usuario.
  - Pantalla de registro de asistencia (de mentor a mentoreados).

- **Tareas:**
  - Diseñar el logo de la aplicación.
  - Crear maquetas de:
    - Pantalla de inicio de cada usuario.
    - Pantalla de registro de asistencia (de mentor a mentoreados).
