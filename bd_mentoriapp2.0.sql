create table escuelas (
    escuela_id serial primary key,
    nombre varchar(255)
);

create table usuarios (
    user_id serial primary key, -- dni
    dni_usuario varchar(20) not null,
    nombre_usuario varchar(255) not null,
    apellido_usuario varchar(255) not null,
    celular_usuario varchar(15) not null,
    password_hash varchar(255) not null,
    escuela_id int not null,
    semestre varchar(5) check (semestre in ('I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII')),
    email varchar(255) not null, -- agregar edad
    tipo_usuario varchar(20) check (tipo_usuario in ('psicologia', 'coordinador', 'mentor', 'mentoriado')) not null,
    foreign key (escuela_id) references escuelas(escuela_id) on delete cascade,
    creado_en timestamp default current_timestamp,
    unique(dni_usuario)
);

create table psicologia (
    psicologia_id serial primary key,
    user_id int not null,
    foreign key (user_id) references usuarios(user_id) on delete cascade
);


create table coordinadores (
    coordinador_id serial primary key,
    user_id int not null,
    foreign key (user_id) references usuarios(user_id) on delete cascade
);

create table mentores (
    mentor_id serial primary key,
    user_id int not null,
    foreign key (user_id) references usuarios(user_id) on delete cascade
);

create table mentoriados (
    mentoriado_id serial primary key,
    user_id int not null,
    foreign key (user_id) references usuarios(user_id) on delete cascade
);

create table horarios (
    horario_id serial primary key,
    lugar varchar(255),
    dia varchar(15) check (dia in ('Lunes', 'Martes', 'Miercoles', 'Jueves', 'Viernes', 'Sabado')),
    hora_inicio time,
    hora_fin time,
    estado boolean
);

create table grupos (
    grupo_id serial primary key,
    jefe_id int not null,
    nombre varchar(255),
    horario_id int not null,
    descripcion text null,
    creado_en timestamp default current_timestamp,
    foreign key (jefe_id) references usuarios(user_id) on delete cascade,
    foreign key (horario_id) references horarios(horario_id) on delete cascade
);

create table miembros_grupo (
    miembro_grupo_id serial primary key,
    grupo_id int not null,
    user_id int not null,
    foreign key (grupo_id) references grupos(grupo_id) on delete cascade,
    foreign key (user_id) references usuarios(user_id) on delete cascade
);

create table sesiones_mentoria (
    sesion_id serial primary key,
    grupo_id int not null,
    hora_programada timestamp not null,
    estado varchar(15) check (estado in ('programada', 'realizada', 'cancelada')) not null,
    tema_sesion varchar(100) not null,
    notas text null,
    fotografia bytea not null,
    foreign key (grupo_id) references grupos(grupo_id) on delete cascade
);

create table asistencias_sesiones (
    asistencia_id serial primary key,
    sesion_id int not null,
    mentoriado_id int not null,
    asistio boolean not null,
    hora_fecha_registrada timestamp default current_timestamp,
    foreign key (sesion_id) references sesiones_mentoria(sesion_id) on delete cascade,
    foreign key (mentoriado_id) references mentoriados(mentoriado_id) on delete cascade
);

create table solicitudes_mentoria (
    solicitud_id serial primary key,
    psicologia_id int not null,
    mentor_id int not null,
    fecha_solicitud timestamp default current_timestamp,
    estado varchar(15) check (estado in ('pendiente', 'aceptada', 'rechazada')) not null,
    mensaje text not null, /* agregar tipos de solicitudes */
    foreign key (psicologia_id) references psicologia(psicologia_id) on delete cascade,
    foreign key (mentor_id) references mentores(mentor_id) on delete cascade
);

create table evaluaciones_comentarios (
    evaluacion_id serial primary key,
    evaluado_id int not null,
    evaluador_id int not null,
    puntuacion_categorica varchar(15) check (puntuacion_categorica in ('Destacable', 'Pasable', 'Bajo')) not null,
    comentario text,
    foreign key (evaluado_id) references usuarios(user_id) on delete cascade,
    foreign key (evaluador_id) references usuarios(user_id) on delete cascade
);

create table mensajes_grupo (
    mensaje_id serial primary key,
    grupo_id int not null,
    remitente_id int not null,
    texto_mensaje text not null,
    enviado_en timestamp default current_timestamp,
    foreign key (grupo_id) references grupos(grupo_id) on delete cascade,
    foreign key (remitente_id) references usuarios(user_id) on delete cascade
);

create table eventos (
    evento_id serial primary key,
    nombre varchar(255) not null,
    horario_id int not null,
    descripcion text null,
    poster bytea not null,
    url varchar(255) null,
    foreign key (horario_id) references horarios(horario_id) on delete cascade
);

create table notificaciones (
    notificacion_id serial primary key,
    user_id int not null,
    texto_notificacion text not null,
    tipo_notificacion varchar(20) check (tipo_notificacion in ('alerta', 'recordatorio', 'otro')) not null,
    creado_en timestamp default current_timestamp,
    leido boolean default false,
    evento_id int not null,
    foreign key (user_id) references usuarios(user_id) on delete cascade,
    foreign key (evento_id) references eventos(evento_id) on delete cascade
);


-- Función para manejar la lógica de inserción según el tipo de usuario
CREATE OR REPLACE FUNCTION insertar_usuario_tipo() RETURNS TRIGGER AS $$
BEGIN
    -- Caso para psicología
    IF NEW.tipo_usuario = 'psicologia' THEN
        -- Insertamos en la tabla psicologia
        INSERT INTO psicologia (user_id) VALUES (NEW.user_id);

    -- Caso para coordinador
    ELSIF NEW.tipo_usuario = 'coordinador' THEN
        -- Insertamos en la tabla coordinadores
        INSERT INTO coordinadores (user_id) VALUES (NEW.user_id);

    -- Caso para mentor
    ELSIF NEW.tipo_usuario = 'mentor' THEN
        -- Insertamos en la tabla mentores
        INSERT INTO mentores (user_id) VALUES (NEW.user_id);

    -- Caso para mentoriado
    ELSIF NEW.tipo_usuario = 'mentoriado' THEN
        -- Permitimos que escuela_id sea NULL si no se proporciona
        INSERT INTO mentoriados (user_id) VALUES (NEW.user_id);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger que llama a la función después de la inserción en la tabla usuarios
CREATE TRIGGER trigger_insertar_usuario_tipo
AFTER INSERT ON usuarios
FOR EACH ROW
EXECUTE FUNCTION insertar_usuario_tipo();

INSERT INTO public.horarios (lugar, dia, hora_inicio, hora_fin)
VALUES
('Aula A-204', 'Lunes', '09:00:00', '11:00:00'),
('Sala de Conferencias B', 'Martes', '14:00:00', '16:00:00'),
('Aula 101', 'Miercoles', '08:00:00', '10:00:00'),
('Auditorio', 'Jueves', '15:30:00', '17:30:00'),
('Sala de Reuniones', 'Viernes', '12:00:00', '14:00:00');

INSERT INTO escuelas (nombre) VALUES ('Derecho');
INSERT INTO escuelas (nombre) VALUES ('Ingeniería de Software');
INSERT INTO escuelas (nombre) VALUES ('Psicología');
INSERT INTO escuelas (nombre) VALUES ('Administración y Negocios Internacionales');
INSERT INTO escuelas (nombre) VALUES ('Ciencias de la Comunicación');
INSERT INTO escuelas (nombre) VALUES ('Ingeniería Comercial');
INSERT INTO escuelas (nombre) VALUES ('Arquitectura');
INSERT INTO escuelas (nombre) VALUES ('Ingeniería Industrial');

INSERT INTO usuarios (dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, password_hash, escuela_id, semestre, email, tipo_usuario)
VALUES ('72866150', 'Pepita', 'Martínez', '996666666', '12345', 1, 'I', 'ana.martinez@gmail.com', 'coordinador');


INSERT INTO usuarios (dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, password_hash, escuela_id, semestre, email, tipo_usuario)
VALUES ('12345678', 'Pepita', 'Martínez', '996666666', '12345', 1, 'I', 'ana.martinez@gmail.com', 'psicologia');

INSERT INTO usuarios (dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, password_hash, escuela_id, semestre, email, tipo_usuario)
VALUES ('87654321', 'Sofía', 'Ramírez', '991111111', '12345', 1, 'III', 'sofia.ramirez@gmail.com', 'coordinador');

INSERT INTO usuarios (dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, password_hash, escuela_id, semestre, email, tipo_usuario)
VALUES ('23232323', 'Luisa', 'Mamani', '991111111', '12345', 1, 'III', 'sofia.ramirez@gmail.com', 'mentor');

INSERT INTO eventos (
    nombre,
    horario_id,
    descripcion,
    poster,
    url,
    fecha_evento
)
VALUES (
    'Conferencia de Tecnología',        -- nombre
    2,                                  -- horario_id (debe existir en la tabla 'horarios')
    'Un evento sobre las últimas tendencias en tecnología.',  -- descripcion
    decode('iVBORw0KGgoAAAANSUhEUgAAAAUA', 'base64'),  -- poster en Base64
    'https://www.example.com/evento',   -- url
    '2024-12-15'                        -- fecha_evento (formato 'YYYY-MM-DD')
);


INSERT INTO usuarios (dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, password_hash, escuela_id, semestre, email, tipo_usuario)
VALUES
    ('10101010', 'María', 'García', '911111111', 'pass1', 1, 'I', 'maria.garcia@gmail.com', 'mentoriado'),
    ('20202020', 'José', 'Fernández', '912222222', 'pass2', 1, 'II', 'jose.fernandez@gmail.com', 'mentoriado'),
    ('30303030', 'Ana', 'Sánchez', '913333333', 'pass3', 1, 'III', 'ana.sanchez@gmail.com', 'mentoriado'),
    ('40404040', 'Luis', 'Ramírez', '914444444', 'pass4', 1, 'IV', 'luis.ramirez@gmail.com', 'mentoriado'),
    ('50505050', 'Laura', 'Torres', '915555555', 'pass5', 1, 'I', 'laura.torres@gmail.com', 'mentoriado'),
    ('60606060', 'David', 'Hernández', '916666666', 'pass6', 1, 'II', 'david.hernandez@gmail.com', 'mentoriado'),
    ('70707070', 'Claudia', 'Morales', '917777777', 'pass7', 1, 'III', 'claudia.morales@gmail.com', 'mentoriado'),
    ('80808080', 'Ricardo', 'Díaz', '918888888', 'pass8', 1, 'IV', 'ricardo.diaz@gmail.com', 'mentoriado'),
    ('90909090', 'Carmen', 'Jiménez', '919999999', 'pass9', 1, 'I', 'carmen.jimenez@gmail.com', 'mentoriado'),
    ('11111111', 'Fernando', 'Cruz', '920000000', 'pass10', 1, 'II', 'fernando.cruz@gmail.com', 'mentoriado');

INSERT INTO usuarios (dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, password_hash, escuela_id, semestre, email, tipo_usuario)
VALUES
    ('22222222', 'Valeria', 'Pérez', '921111111', 'pass11', 2, 'I', 'valeria.perez@gmail.com', 'mentoriado'),
    ('33333333', 'Mateo', 'Salas', '922222222', 'pass12', 2, 'II', 'mateo.salas@gmail.com', 'mentoriado'),
    ('44444444', 'Natalia', 'Moreno', '923333333', 'pass13', 2, 'III', 'natalia.moreno@gmail.com', 'mentoriado'),
    ('55555555', 'Sebastián', 'Rivas', '924444444', 'pass14', 2, 'IV', 'sebastian.rivas@gmail.com', 'mentoriado'),
    ('66666666', 'Isabella', 'Vega', '925555555', 'pass15', 2, 'I', 'isabella.vega@gmail.com', 'mentoriado'),
    ('77777777', 'Andrés', 'Aguilar', '926666666', 'pass16', 2, 'II', 'andres.aguilar@gmail.com', 'mentoriado'),
    ('88888888', 'Camila', 'Castillo', '927777777', 'pass17', 2, 'III', 'camila.castillo@gmail.com', 'mentoriado'),
    ('99999999', 'Diego', 'Ruiz', '928888888', 'pass18', 2, 'IV', 'diego.ruiz@gmail.com', 'mentoriado'),
    ('10101012', 'Paola', 'López', '929999999', 'pass19', 2, 'I', 'paola.lopez@gmail.com', 'mentoriado'),
    ('20202022', 'Joaquín', 'Mendoza', '930000000', 'pass20', 2, 'II', 'joaquin.mendoza@gmail.com', 'mentoriado');


INSERT INTO sesiones_mentoria (grupo_id, hora_programada, estado, tema_sesion, notas, fotografia)
VALUES
(1, '2024-11-07 12:00:00', 'programada', 'Desarrollo personal', 'Notas de la sesión 1', E'\\x496d6167656e53696d756c616465'),  -- Imagen simulada en formato binario
(1, '2024-11-08 12:00:00', 'realizada', 'Técnicas de comunicación', 'Notas de la sesión 2', E'\\x496d6167656e53696d756c616465'),
(1, '2024-11-09 12:00:00', 'cancelada', 'Gestión de proyectos', 'Notas de la sesión 3', E'\\x496d6167656e53696d756c616465'),
(1, '2024-11-10 12:00:00', 'programada', 'Liderazgo', 'Notas de la sesión 4', E'\\x496d6167656e53696d756c616465'),
(1, '2024-11-11 12:00:00', 'realizada', 'Mindfulness', 'Notas de la sesión 5', E'\\x496d6167656e53696d756c616465'),
(1, '2024-11-12 12:00:00', 'programada', 'Trabajo en equipo', 'Notas de la sesión 6', E'\\x496d6167656e53696d756c616465');


-- Asistencias para la sesión 1
INSERT INTO asistencias_sesiones (sesion_id, mentoriado_id, asistio, hora_fecha_registrada)
VALUES
(1, 10, TRUE, '2024-11-07 11:45:00'),
(1, 11, FALSE, '2024-11-07 11:50:00'),
(1, 12, TRUE, '2024-11-07 11:55:00'),
(1, 13, FALSE, '2024-11-07 12:00:00'),
(1, 14, TRUE, '2024-11-07 12:05:00'),
(1, 15, FALSE, '2024-11-07 12:10:00'),
(1, 16, TRUE, '2024-11-07 12:15:00'),
(1, 17, FALSE, '2024-11-07 12:20:00'),
(1, 18, TRUE, '2024-11-07 12:25:00'),
(1, 19, FALSE, '2024-11-07 12:30:00');

-- Asistencias para la sesión 2
INSERT INTO asistencias_sesiones (sesion_id, mentoriado_id, asistio, hora_fecha_registrada)
VALUES
(2, 10, TRUE, '2024-11-08 11:45:00'),
(2, 11, FALSE, '2024-11-08 11:50:00'),
(2, 12, TRUE, '2024-11-08 11:55:00'),
(2, 13, FALSE, '2024-11-08 12:00:00'),
(2, 14, TRUE, '2024-11-08 12:05:00'),
(2, 15, FALSE, '2024-11-08 12:10:00'),
(2, 16, TRUE, '2024-11-08 12:15:00'),
(2, 17, FALSE, '2024-11-08 12:20:00'),
(2, 18, TRUE, '2024-11-08 12:25:00'),
(2, 19, FALSE, '2024-11-08 12:30:00');

-- Asistencias para la sesión 3
INSERT INTO asistencias_sesiones (sesion_id, mentoriado_id, asistio, hora_fecha_registrada)
VALUES
(3, 10, FALSE, '2024-11-09 11:45:00'),
(3, 11, TRUE, '2024-11-09 11:50:00'),
(3, 12, FALSE, '2024-11-09 11:55:00'),
(3, 13, TRUE, '2024-11-09 12:00:00'),
(3, 14, FALSE, '2024-11-09 12:05:00'),
(3, 15, TRUE, '2024-11-09 12:10:00'),
(3, 16, FALSE, '2024-11-09 12:15:00'),
(3, 17, TRUE, '2024-11-09 12:20:00'),
(3, 18, FALSE, '2024-11-09 12:25:00'),
(3, 19, TRUE, '2024-11-09 12:30:00');

-- Repite este patrón para las demás sesiones (4, 5, 6) con los datos correspondientes.
-- Asistencias para la sesión 4
INSERT INTO asistencias_sesiones (sesion_id, mentoriado_id, asistio, hora_fecha_registrada)
VALUES
(4, 10, TRUE, '2024-11-10 11:45:00'),
(4, 11, FALSE, '2024-11-10 11:50:00'),
(4, 12, TRUE, '2024-11-10 11:55:00'),
(4, 13, FALSE, '2024-11-10 12:00:00'),
(4, 14, TRUE, '2024-11-10 12:05:00'),
(4, 15, FALSE, '2024-11-10 12:10:00'),
(4, 16, TRUE, '2024-11-10 12:15:00'),
(4, 17, FALSE, '2024-11-10 12:20:00'),
(4, 18, TRUE, '2024-11-10 12:25:00'),
(4, 19, FALSE, '2024-11-10 12:30:00');


-- Asistencias para la sesión 5
INSERT INTO asistencias_sesiones (sesion_id, mentoriado_id, asistio, hora_fecha_registrada)
VALUES
(5, 10, FALSE, '2024-11-11 11:45:00'),
(5, 11, TRUE, '2024-11-11 11:50:00'),
(5, 12, FALSE, '2024-11-11 11:55:00'),
(5, 13, TRUE, '2024-11-11 12:00:00'),
(5, 14, FALSE, '2024-11-11 12:05:00'),
(5, 15, TRUE, '2024-11-11 12:10:00'),
(5, 16, FALSE, '2024-11-11 12:15:00'),
(5, 17, TRUE, '2024-11-11 12:20:00'),
(5, 18, FALSE, '2024-11-11 12:25:00'),
(5, 19, TRUE, '2024-11-11 12:30:00');


-- Asistencias para la sesión 6
INSERT INTO asistencias_sesiones (sesion_id, mentoriado_id, asistio, hora_fecha_registrada)
VALUES
(6, 10, TRUE, '2024-11-12 11:45:00'),
(6, 11, FALSE, '2024-11-12 11:50:00'),
(6, 12, TRUE, '2024-11-12 11:55:00'),
(6, 13, FALSE, '2024-11-12 12:00:00'),
(6, 14, TRUE, '2024-11-12 12:05:00'),
(6, 15, FALSE, '2024-11-12 12:10:00'),
(6, 16, TRUE, '2024-11-12 12:15:00'),
(6, 17, FALSE, '2024-11-12 12:20:00'),
(6, 18, TRUE, '2024-11-12 12:25:00'),
(6, 19, FALSE, '2024-11-12 12:30:00');
