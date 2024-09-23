
create table usuarios (
    user_id int auto_increment primary key, -- dni
    dni_usuario int not null,
    nombre_usuario varchar(255) not null,
    apellido_usuario varchar(255) not null,
    celular_usuario smallint not null,
    password_hash varchar(255) not null,
	semestre enum('I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII') null,
    email varchar(255) not null, -- agregar edad
    tipo_usuario enum('psicología', 'coordinador', 'mentor', 'mentoriado') not null,
    creado_en datetime default current_timestamp,
    unique(dni_usuario)
);

create table psicologia (
    psicologia_id int auto_increment primary key,
    user_id int not null,
    cargo varchar(255),
    foreign key (user_id) references usuarios(user_id)
);

create table escuelas (
    escuela_id int auto_increment primary key,
    nombre varchar(255)
);

create table coordinadores (
    coordinador_id int auto_increment primary key,
    user_id int not null,
    escuela_id int not null,
    foreign key (escuela_id) references escuelas(escuela_id),
    foreign key (user_id) references usuarios(user_id)
);

create table mentores (
    mentor_id int auto_increment primary key,
    user_id int not null,
    escuela_id int not null,
    foreign key (escuela_id) references escuelas(escuela_id),
    foreign key (user_id) references usuarios(user_id)
);

create table mentoriados (
    mentoriado_id int auto_increment primary key,
    user_id int not null,
    escuela_id int,
    foreign key (escuela_id) references escuelas(escuela_id),
    foreign key (user_id) references usuarios(user_id)
);

create table horarios (
    horario_id int auto_increment primary key,
    lugar varchar(255),
    dia enum('Lunes', 'Martes', 'Miercoles', 'Jueves', 'Viernes', 'Sabado'),
    hora_inicio time,
    hora_fin time,
    estado boolean
);

create table grupos_mentoria (
    grupo_id int auto_increment primary key,
    mentor_id int not null,
    nombre varchar(255),
    horario_id int not null,
    descripcion text null,
    creado_en datetime default current_timestamp,
    foreign key (mentor_id) references mentores(mentor_id),
    foreign key (horario_id) references horarios(horario_id)
);

create table miembros_grupo (
    miembro_grupo_id int auto_increment primary key,
    grupo_id int not null,
    user_id int not null,
    rol enum('mentor', 'mentoriado') not null,
    foreign key (grupo_id) references grupos_mentoria(grupo_id),
    foreign key (user_id) references usuarios(user_id)
);

create table sesiones_mentoria (
    sesion_id int auto_increment primary key,
    grupo_id int not null,
    hora_programada datetime not null,
    estado enum('programada', 'realizada', 'cancelada') not null,
    tema_sesion varchar(100) not null,
    notas text null,
    fotografia longblob not null,
    foreign key (grupo_id) references grupos_mentoria(grupo_id)
);

create table asistencias_sesiones (
    asistencia_id int auto_increment primary key,
    sesion_id int not null,
    mentoriado_id int not null,
    asistio boolean not null,
    hora_fecha_regitrada datetime default current_timestamp,
    foreign key (sesion_id) references sesiones_mentoria(sesion_id),
    foreign key (mentoriado_id) references mentoriados(mentoriado_id)
);

create table solicitudes_mentoria (
    solicitud_id int auto_increment primary key,
    coordinador_id int not null,
    mentor_id int not null,
    fecha_solicitud datetime default current_timestamp,
    estado enum('pendiente', 'aceptada', 'rechazada') not null,
    mensaje text not null, /* agregar tipos de solicitudes */
    foreign key (coordinador_id) references coordinadores(coordinador_id),
    foreign key (mentor_id) references mentores(mentor_id)
);

create table evaluaciones_comentarios (
    evaluacion_id int auto_increment primary key,
    evaluado_id int not null,
    evaluador_id int not null,
    puntuacion_categorica enum('Destacable', 'Pasable', 'Bajo') not null,
    comentario text,
    foreign key (evaluado_id) references usuarios(user_id),
    foreign key (evaluador_id) references usuarios(user_id)
);

create table mensajes_grupo (
    mensaje_id int auto_increment primary key,
    grupo_id int not null,
    remitente_id int not null,
    texto_mensaje text not null,
    enviado_en datetime default current_timestamp,
    foreign key (grupo_id) references grupos_mentoria(grupo_id),
    foreign key (remitente_id) references usuarios(user_id)
);

create table eventos (
	evento_id int auto_increment primary key,
	nombre varchar(255) not null,
    horario_id int not null,
    descripcion text null,
	poster longblob not null,
    url varchar(255) null,
    foreign key (horario_id) references horarios(horario_id)
);

create table notificaciones (
    notificacion_id int auto_increment primary key,
    user_id int not null,
    texto_notificacion text not null,
    tipo_notificacion enum('alerta', 'recordatorio', 'otro') not null,
    creado_en datetime default current_timestamp,
    leido boolean default false,
    evento_id int not null,
    foreign key (user_id) references usuarios(user_id),
    foreign key (evento_id) references eventos(evento_id)
);



