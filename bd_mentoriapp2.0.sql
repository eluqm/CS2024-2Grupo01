create table usuarios (
    user_id serial primary key, -- dni
    dni_usuario varchar(20) not null,
    nombre_usuario varchar(255) not null,
    apellido_usuario varchar(255) not null,
    celular_usuario varchar(15) not null,
    password_hash varchar(255) not null,
    semestre varchar(5) check (semestre in ('I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII')),
    email varchar(255) not null, -- agregar edad
    tipo_usuario varchar(20) check (tipo_usuario in ('psicología', 'coordinador', 'mentor', 'mentoriado')) not null,
    creado_en timestamp default current_timestamp,
    unique(dni_usuario)
);

create table psicologia (
    psicologia_id serial primary key,
    user_id int not null,
    cargo varchar(255),
    foreign key (user_id) references usuarios(user_id) on delete cascade
);

create table escuelas (
    escuela_id serial primary key,
    nombre varchar(255)
);

create table coordinadores (
    coordinador_id serial primary key,
    user_id int not null,
    escuela_id int not null,
    foreign key (escuela_id) references escuelas(escuela_id) on delete cascade,
    foreign key (user_id) references usuarios(user_id) on delete cascade
);

create table mentores (
    mentor_id serial primary key,
    user_id int not null,
    escuela_id int not null,
    foreign key (escuela_id) references escuelas(escuela_id) on delete cascade,
    foreign key (user_id) references usuarios(user_id) on delete cascade
);

create table mentoriados (
    mentoriado_id serial primary key,
    user_id int not null,
    escuela_id int,
    foreign key (escuela_id) references escuelas(escuela_id) on delete set null,
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

create table grupos_mentoria (
    grupo_id serial primary key,
    mentor_id int not null,
    nombre varchar(255),
    horario_id int not null,
    descripcion text null,
    creado_en timestamp default current_timestamp,
    foreign key (mentor_id) references mentores(mentor_id) on delete cascade,
    foreign key (horario_id) references horarios(horario_id) on delete cascade
);

create table miembros_grupo (
    miembro_grupo_id serial primary key,
    grupo_id int not null,
    user_id int not null,
    foreign key (grupo_id) references grupos_mentoria(grupo_id) on delete cascade,
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
    foreign key (grupo_id) references grupos_mentoria(grupo_id) on delete cascade
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
    coordinador_id int not null,
    mentor_id int not null,
    fecha_solicitud timestamp default current_timestamp,
    estado varchar(15) check (estado in ('pendiente', 'aceptada', 'rechazada')) not null,
    mensaje text not null, /* agregar tipos de solicitudes */
    foreign key (coordinador_id) references coordinadores(coordinador_id) on delete cascade,
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
    foreign key (grupo_id) references grupos_mentoria(grupo_id) on delete cascade,
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