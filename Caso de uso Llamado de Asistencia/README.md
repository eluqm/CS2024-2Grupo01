 ---

**Caso de Uso: Llamado de Asistencia**

### 1. Breve Descripción del Caso de Uso  
El caso de uso "Llamado de Asistencia" permite a los mentores registrar la asistencia de los mentoriados en cada sesión. El sistema también permite registrar la asistencia a eventos, adjuntar evidencia como fotografías, y añadir una breve descripción.

### 2. Flujo de Eventos  

#### 2.1 Flujo Básico  
1. El mentor accede a la opción de registrar asistencia en una sesión programada.
2. El sistema muestra el nombre de la sesión, que se genera automáticamente, junto con la fecha y la hora.
3. El mentor puede editar el nombre de la sesión para que resuma el tema de la misma.
4. La asistencia del mentor se marca automáticamente como presente.
5. El sistema marca por defecto que todos los mentoriados han asistido.
6. El mentor desmarca a los mentoriados que no asistieron.
7. El mentor puede adjuntar un enlace o una fotografía como evidencia de la sesión, y añadir una breve descripción (opcional).
8. El sistema registra la asistencia de los mentores y mentoriados, junto con la evidencia adjunta (si la hubiera).

#### 2.2 Flujos Alternativos  
- **Asistencia a eventos:** Si el registro de asistencia es para un evento, el sistema reconocerá automáticamente la fecha y hora del evento y permitirá tomar asistencia en base a esa información.
- **Modificación de asistencia:** El mentor puede modificar la asistencia dentro del período permitido antes de que los registros queden definitivos.

### 3. Precondiciones  
1. La sesión debe haber sido programada previamente.
2. El mentor debe tener permisos para registrar la asistencia de los mentoriados.
3. La sesión debe estar dentro del horario establecido.

### 4. Postcondiciones  
1. La asistencia de la sesión ha sido registrada, incluyendo los mentoriados ausentes y la evidencia adjunta.
2. La información queda almacenada para ser consultada posteriormente.

---
