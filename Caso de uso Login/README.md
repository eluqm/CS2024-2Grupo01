

---

**Caso de Uso: Login**  

### 1. Breve Descripción del Caso de Uso  
El caso de uso "Login" describe la funcionalidad que permite a los usuarios iniciar sesión por primera vez usando su DNI. El sistema valida si el DNI pertenece a la organización y, de ser así, le permite al usuario establecer una contraseña de respaldo.

### 2. Flujo de Eventos  

#### 2.1 Flujo Básico  
1. El usuario ingresa a la pantalla de inicio de sesión.
2. El sistema solicita el ingreso del DNI.
3. El usuario ingresa su DNI.
4. El sistema valida si el DNI está registrado en la base de datos de la organización.
   - Si el DNI es válido, se solicita al usuario crear una contraseña de respaldo.
   - Si el DNI no es válido, se muestra un mensaje de error indicando que no pertenece a la organización.
5. El usuario ingresa una contraseña de respaldo y confirma.
6. El sistema almacena la nueva contraseña y confirma el acceso exitoso.
7. El usuario es redirigido a la pantalla principal del sistema.

#### 2.2 Flujos Alternativos  
- **Error en el DNI:** Si el DNI no está registrado, el sistema muestra un mensaje de error y solicita intentar nuevamente o contactar con soporte.
- **Error en la creación de la contraseña:** Si las contraseñas no coinciden o no cumplen con los criterios de seguridad, el sistema solicitará que se ingrese nuevamente.

### 3. Precondiciones  
1. El usuario debe tener un DNI válido registrado en la base de datos de la organización.
2. El usuario debe tener acceso a la plataforma.

### 4. Postcondiciones  
1. El usuario ha iniciado sesión y tiene una contraseña de respaldo configurada para futuros accesos.

---
