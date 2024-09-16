
---

**Universidad La Salle**  
**Caso de Uso: Carga Masiva de Datos**  

### 1. Breve Descripción del Caso de Uso

El caso de uso "Carga Masiva de Datos" describe la funcionalidad que permite a los usuarios subir datos de coordinadores, mentores, mentoriados y aulas mediante un archivo CSV, además de ofrecer la posibilidad de ingresar los datos de manera manual. Esta función está dirigida al departamento de Psicología y permite manejar la información de manera eficiente al inicio de cada semestre.

### 2. Flujo de Eventos

#### 2.1 Flujo Básico

1. El usuario selecciona la opción para cargar un archivo CSV.
2. El sistema valida que el formato del archivo sea correcto según la plantilla proporcionada previamente.
3. El sistema carga los datos del CSV, asignando a cada carrera su respectiva información.
4. El usuario también puede optar por ingresar los datos manualmente mediante cuadros de texto para entidades específicas.
5. Los datos subidos estarán disponibles para los usuarios correspondientes (coordinadores, mentores, mentoriados).
6. Una vez que la universidad otorgue acceso a las aulas y horarios, el usuario podrá subir un CSV con la asignación de aulas y horarios para su uso.
7. El sistema permite modificar o agregar nuevas aulas y horarios manualmente.

#### 2.2 Flujos Alternativos

1. Si el archivo CSV no sigue el formato de la plantilla, el sistema mostrará un mensaje de error y pedirá que se corrija el archivo.
2. En caso de datos incompletos o inconsistentes, el sistema ofrecerá la opción de completar o corregir la información manualmente.

### 3. Precondiciones

1. El usuario debe tener acceso al sistema y a los permisos adecuados para cargar y modificar datos.
2. El archivo CSV debe seguir el formato correcto según la plantilla proporcionada.
3. La universidad debe otorgar acceso a las aulas y horarios para poder asignarlos en el sistema.

### 4. Postcondiciones

1. Los datos de coordinadores, mentores y mentoriados estarán correctamente registrados y disponibles en el sistema.
2. Los mentoriados podrán ver las aulas y horarios asignados.
3. El sistema permite actualizaciones manuales en los horarios y aulas cuando sea necesario.

### 5. Consideraciones Especiales

- **Formato de Datos:** Preguntar a la Psicóloga responsable cómo recibe los datos de los estudiantes al inicio del semestre, para ajustar el formato de ingreso masivo y manual.
- **Manejo de CSV:** Se permitirá subir un archivo CSV por carrera o un solo CSV estructurado correctamente para todas las carreras.
- **Horarios y Aulas:** La inserción de aulas y horarios podrá ser manual o mediante carga masiva, y los mentoriados tendrán acceso a esta información publicada.

---
