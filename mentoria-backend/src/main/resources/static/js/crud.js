let currentTable = '';
let dataTable = null;
let currentEditId = null;
let schoolsData = []; // Variable para almacenar los datos de escuelas

// Configuración de campos por tabla
const tableConfigs = {
    '/usuarios': {
        title: 'Usuarios',
        fields: [
            { name: 'userId', type: 'hidden' },
            { 
                name: 'dniUsuario', 
                type: 'text', 
                label: 'DNI', 
                required: true,
                maxLength: 20,
                pattern: '^[0-9]{8}$',
                validationMessage: 'El DNI debe tener 8 dígitos'
            },
            { 
                name: 'nombreUsuario', 
                type: 'text', 
                label: 'Nombre', 
                required: true,
                maxLength: 255
            },
            { 
                name: 'apellidoUsuario', 
                type: 'text', 
                label: 'Apellido', 
                required: true,
                maxLength: 255
            },
            { 
                name: 'celularUsuario', 
                type: 'tel', 
                label: 'Celular', 
                required: true,
                maxLength: 15,
                pattern: '^[0-9]{9}$',
                validationMessage: 'El celular debe tener 9 dígitos'
            },
            { 
                name: 'passwordHash', 
                type: 'password', 
                label: 'Contraseña', 
                required: true,
                minLength: 8,
                validationMessage: 'La contraseña debe tener al menos 8 caracteres'
            },
            { 
                name: 'escuelaId', 
                type: 'select', 
                label: 'Escuela',
                required: true,
                options: [] // Se llenará dinámicamente
            },
            { 
                name: 'semestre', 
                type: 'select', 
                label: 'Semestre', 
                required: false,
                options: ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII']
            },
            { 
                name: 'email', 
                type: 'email', 
                label: 'Email', 
                required: true,
                maxLength: 255
            },
            { 
                name: 'tipoUsuario', 
                type: 'select', 
                label: 'Tipo de Usuario', 
                required: true,
                options: ['psicologia', 'coordinador', 'mentor', 'mentoriado']
            }
        ],
        columns: [
            { data: 'userId', title: 'ID' },
            { data: 'dniUsuario', title: 'DNI' },
            { data: 'nombreUsuario', title: 'Nombre' },
            { data: 'apellidoUsuario', title: 'Apellido' },
            { data: 'celularUsuario', title: 'Celular' },
            { data: 'email', title: 'Email' },
            { data: 'escuelaId', visible: false },
            { 
                data: 'escuelaId', 
                title: 'Escuela', 
                render: function(data, type, row) {
                    // Buscar el nombre de la escuela correspondiente al ID
                    const escuela = schoolsData.find(e => e.escuelaId === parseInt(data));
                    return escuela ? escuela.nombre : data;
                }
            },
            { data: 'tipoUsuario', title: 'Tipo' },
            { data: 'semestre', title: 'Semestre' },
            { 
                data: null, 
                title: 'Acciones',
                render: function(data, type, row) {
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-primary" onclick="editRecord(${row.userId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteRecord(${row.userId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ]
    },
    '/escuelas': {
        title: 'Escuelas',
        fields: [
            { name: 'escuelaId', type: 'hidden' },
            { 
                name: 'nombre', 
                type: 'text', 
                label: 'Nombre', 
                required: true,
                maxLength: 255
            }
        ],
        columns: [
            { data: 'escuelaId', title: 'ID' },
            { data: 'nombre', title: 'Nombre' },
            { 
                data: null,
                title: 'Acciones', 
                render: function(data, type, row) {
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-primary" onclick="editRecord(${row.escuelaId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteRecord(${row.escuelaId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ]
    },
    '/horarios': {
        title: 'Horarios',
        fields: [
            { name: 'horarioId', type: 'hidden' },
            { 
                name: 'lugar', 
                type: 'text', 
                label: 'Lugar', 
                required: true,
                maxLength: 255,
                placeholder: 'Ej: Aula 101, Laboratorio 2'
            },
            { 
                name: 'dia', 
                type: 'select', 
                label: 'Día', 
                required: true,
                options: ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']
            },
            { 
                name: 'horaInicio', 
                type: 'time', 
                label: 'Hora Inicio', 
                required: true,
                min: '06:00',
                max: '22:00'
            },
            { 
                name: 'horaFin', 
                type: 'time', 
                label: 'Hora Fin', 
                required: true,
                min: '06:00',
                max: '22:00'
            },
            { 
                name: 'estado', 
                type: 'select', 
                label: 'Estado', 
                required: true,
                options: [
                    { value: 'true', text: 'Activo' },
                    { value: 'false', text: 'Inactivo' }
                ]
            },
            { 
                name: 'nombreGrupo', 
                type: 'text', 
                label: 'Nombre del Grupo', 
                required: true,
                maxLength: 255,
                placeholder: 'Ej: Grupo A, Grupo B'
            },
            { 
                name: 'nombreCompletoJefe', 
                type: 'text', 
                label: 'Nombre del Jefe', 
                required: true,
                maxLength: 255,
                placeholder: 'Nombre completo del jefe de grupo'
            },
            { 
                name: 'nombreEscuela', 
                type: 'text', 
                label: 'Nombre de la Escuela', 
                required: true,
                maxLength: 255,
                placeholder: 'Nombre completo de la escuela'
            }
        ],
        columns: [
            { data: 'horarioId', title: 'ID', visible: false },
            { data: 'nombreGrupo', title: 'Grupo' },
            { data: 'nombreCompletoJefe', title: 'Mentor' },
            { data: 'nombreEscuela', title: 'Carrera' },
            { data: 'lugar', title: 'Lugar' },
            { data: 'dia', title: 'Día' },
            { data: 'horaInicio', title: 'Hora Inicio' },
            { data: 'horaFin', title: 'Hora Fin' },
            { 
                data: 'estado', 
                title: 'Estado',
                render: function(data) {
                    return data ? '<span class="badge bg-success">Activo</span>' : '<span class="badge bg-danger">Inactivo</span>';
                }
            },
            { 
                data: null, 
                title: 'Acciones',
                render: function(data, type, row) {
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-primary" onclick="editRecord(${row.horarioId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteRecord(${row.horarioId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ]
    },
    '/grupos': {
        title: 'Grupos',
        fields: [
            { name: 'grupoId', type: 'hidden' },
            { 
                name: 'nombre', 
                type: 'text', 
                label: 'Nombre del Grupo', 
                required: true,
                maxLength: 255
            },
            { 
                name: 'jefeId', 
                type: 'select', 
                label: 'Mentor', 
                required: true,
                options: [] // Se llenará dinámicamente con los mentores
            },
            { 
                name: 'descripcion', 
                type: 'textarea', 
                label: 'Descripción', 
                required: false,
                maxLength: 1000
            }
        ],
        columns: [
            { data: 'grupoId', title: 'ID', visible: false },
            { data: 'nombre', title: 'Nombre del Grupo' },
            { data: 'jefeName', title: 'Mentor' },
            { data: 'descripcion', title: 'Descripción' },
            { data: 'creadoEn', title: 'Fecha de Creación' },
            { 
                data: null, 
                title: 'Acciones',
                render: function(data, type, row) {
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-primary" onclick="editRecord(${row.grupoId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteRecord(${row.grupoId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ]
    },
    '/sesiones_mentoria': {
        title: 'Sesiones de Mentoría',
        fields: [
            { name: 'sesionId', type: 'hidden' },
            { 
                name: 'grupoId', 
                type: 'select', 
                label: 'Grupo', 
                required: true,
                options: [] // Se llenará dinámicamente con los grupos
            },
            { 
                name: 'estado', 
                type: 'select', 
                label: 'Estado', 
                required: true,
                options: [
                    { value: 'pendiente', text: 'Pendiente' },
                    { value: 'en_progreso', text: 'En Progreso' },
                    { value: 'completada', text: 'Completada' },
                    { value: 'cancelada', text: 'Cancelada' }
                ]
            },
            { 
                name: 'temaSesion', 
                type: 'text', 
                label: 'Tema de la Sesión', 
                required: true,
                maxLength: 255
            },
            { 
                name: 'notas', 
                type: 'textarea', 
                label: 'Notas', 
                required: false,
                maxLength: 1000
            },
            { 
                name: 'fotografia', 
                type: 'file', 
                label: 'Fotografía', 
                required: true,
                accept: 'image/*'
            }
        ],
        columns: [
            { data: 'sesionId', title: 'ID', visible: false },
            { data: 'grupoId', title: 'ID Grupo', visible: false },
            { 
                data: 'grupoId', 
                title: 'Grupo',
                render: function(data, type, row) {
                    // Se llenará dinámicamente con el nombre del grupo
                    return data;
                }
            },
            { 
                data: 'estado', 
                title: 'Estado',
                render: function(data) {
                    const estados = {
                        'pendiente': '<span class="badge bg-warning">Pendiente</span>',
                        'en_progreso': '<span class="badge bg-info">En Progreso</span>',
                        'completada': '<span class="badge bg-success">Completada</span>',
                        'cancelada': '<span class="badge bg-danger">Cancelada</span>'
                    };
                    return estados[data] || data;
                }
            },
            { data: 'temaSesion', title: 'Tema' },
            { data: 'notas', title: 'Notas' },
            { 
                data: 'fotografia', 
                title: 'Fotografía',
                render: function(data) {
                    if (data) {
                        return `<img src="data:image/jpeg;base64,${data}" style="max-width: 100px; max-height: 100px;" />`;
                    }
                    return 'Sin imagen';
                }
            },
            { 
                data: null, 
                title: 'Acciones',
                render: function(data, type, row) {
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-primary" onclick="editRecord(${row.sesionId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteRecord(${row.sesionId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ]
    },
    '/readAllEventos': {
        title: 'Eventos',
        fields: [
            { name: 'eventoId', type: 'hidden' },
            { 
                name: 'nombre', 
                type: 'text', 
                label: 'Nombre del Evento', 
                required: true,
                maxLength: 255
            },
            { 
                name: 'horarioId', 
                type: 'select', 
                label: 'Horario', 
                required: true,
                options: [] // Se llenará dinámicamente con los horarios
            },
            { 
                name: 'descripcion', 
                type: 'textarea', 
                label: 'Descripción', 
                required: false,
                maxLength: 1000
            },
            { 
                name: 'poster', 
                type: 'file', 
                label: 'Poster', 
                required: true,
                accept: 'image/*'
            },
            { 
                name: 'url', 
                type: 'url', 
                label: 'URL', 
                required: false,
                maxLength: 255
            },
            { 
                name: 'fecha_evento', 
                type: 'date', 
                label: 'Fecha del Evento', 
                required: true
            }
        ],
        columns: [
            { data: 'eventoId', title: 'ID', visible: false },
            { data: 'nombre', title: 'Nombre' },
            { data: 'horarioId', title: 'ID Horario', visible: false },
            { 
                data: 'horarioId', 
                title: 'Horario',
                render: function(data, type, row) {
                    // Se llenará dinámicamente con la información del horario
                    return data;
                }
            },
            { data: 'descripcion', title: 'Descripción' },
            { 
                data: 'poster', 
                title: 'Poster',
                render: function(data) {
                    if (data) {
                        return `<img src="data:image/jpeg;base64,${data}" style="max-width: 100px; max-height: 100px;" />`;
                    }
                    return 'Sin imagen';
                }
            },
            { data: 'url', title: 'URL' },
            { data: 'fecha_evento', title: 'Fecha' },
            { 
                data: null, 
                title: 'Acciones',
                render: function(data, type, row) {
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-primary" onclick="editRecord(${row.eventoId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteRecord(${row.eventoId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ]
    },
    '/asistencias_sesiones': {
        title: 'Asistencias a Sesiones',
        fields: [
            { name: 'asistenciaId', type: 'hidden' },
            { 
                name: 'sesionId', 
                type: 'select', 
                label: 'Sesión', 
                required: true,
                options: [] // Se llenará dinámicamente con las sesiones
            },
            { 
                name: 'mentoriadoId', 
                type: 'select', 
                label: 'Mentoriado', 
                required: true,
                options: [] // Se llenará dinámicamente con los mentoriados
            },
            { 
                name: 'asistio', 
                type: 'select', 
                label: 'Asistió', 
                required: true,
                options: [
                    { value: 'true', text: 'Sí' },
                    { value: 'false', text: 'No' }
                ]
            }
        ],
        columns: [
            { data: 'asistenciaId', title: 'ID', visible: false },
            { data: 'sesionId', title: 'ID Sesión', visible: false },
            { 
                data: 'sesionId', 
                title: 'Sesión',
                render: function(data, type, row) {
                    // Se llenará dinámicamente con la información de la sesión
                    return data;
                }
            },
            { data: 'mentoriadoId', title: 'ID Mentoriado', visible: false },
            { 
                data: 'mentoriadoId', 
                title: 'Mentoriado',
                render: function(data, type, row) {
                    // Se llenará dinámicamente con la información del mentoriado
                    return data;
                }
            },
            { 
                data: 'asistio', 
                title: 'Asistió',
                render: function(data) {
                    return data ? '<span class="badge bg-success">Sí</span>' : '<span class="badge bg-danger">No</span>';
                }
            },
            { 
                data: 'horaFechaRegistrada', 
                title: 'Fecha/Hora Registro',
                render: function(data) {
                    if (data) {
                        const date = new Date(data);
                        return date.toLocaleString();
                    }
                    return 'No registrado';
                }
            },
            { 
                data: null, 
                title: 'Acciones',
                render: function(data, type, row) {
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-primary" onclick="editRecord(${row.asistenciaId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteRecord(${row.asistenciaId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ]
    },

    // Agregar configuraciones para otras tablas...
};

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    currentTable = urlParams.get('table');
    const relatedTables = urlParams.get('relatedTables');
    
    if (!currentTable || !tableConfigs[currentTable]) {
        window.location.href = 'dashboard.html';
        return;
    }

    document.getElementById('pageTitle').textContent = tableConfigs[currentTable].title;
    document.getElementById('tableTitle').textContent = tableConfigs[currentTable].title;

    // Si estamos en la tabla usuarios o si tenemos tablas relacionadas
    if (currentTable === '/usuarios' || (relatedTables && relatedTables.includes('/escuelas'))) {
        // Primero cargamos los datos de escuelas
        loadSchoolsData().then(() => {
            initializeTable();
            generateFilters();
            generateForm();
            loadData();
        });
    } else {
        initializeTable();
        generateFilters();
        generateForm();
        loadData();
    }
});

// Cargar datos de escuelas
async function loadSchoolsData() {
    try {
        const response = await fetch('/escuelas');
        if (!response.ok) throw new Error('Error al cargar datos de escuelas');
        
        schoolsData = await response.json();
        
        // Actualizar las opciones del campo escuelaId en el formulario de usuarios
        if (tableConfigs['/usuarios'] && tableConfigs['/usuarios'].fields) {
            const escuelaField = tableConfigs['/usuarios'].fields.find(f => f.name === 'escuelaId');
            if (escuelaField) {
                escuelaField.options = schoolsData.map(school => ({
                    value: school.escuelaId.toString(),
                    text: school.nombre
                }));
            }
        }
    } catch (error) {
        showAlert('Error al cargar datos de escuelas: ' + error.message, 'danger');
    }
}

// Inicializar DataTable
function initializeTable() {
    dataTable = $('#dataTable').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/1.11.5/i18n/es-ES.json'
        },
        pageLength: 10,
        order: [[0, 'desc']],
        columns: tableConfigs[currentTable].columns,
        columnDefs: [
            { targets: -1, orderable: false }
        ]
    });
}

// Generar filtros
function generateFilters() {
    const filtersContainer = document.getElementById('filters');
    filtersContainer.innerHTML = '';

    tableConfigs[currentTable].fields.forEach(field => {
        if (field.type === 'password'){
            return; 
        }
        if (field.type !== 'hidden') {
            const col = document.createElement('div');
            col.className = 'col-md-3 mb-3';
            
            let input = '';
            if (field.type === 'select') {
                // Caso especial para el filtro de escuelas
                if (field.name === 'escuelaId' && currentTable === '/usuarios') {
                    console.log("cola");
                    input = `
                        <select class="form-select filter-input" data-field="${field.name}">
                            <option value="">Todas</option>
                            ${schoolsData.map(school => 
                                `<option value="${school.escuelaId}">${school.nombre}</option>`
                            ).join('')}
                        </select>
                    `;
                } else {
                    // Para otros selects
                    const options = Array.isArray(field.options) 
                        ? field.options.map(opt => typeof opt === 'object' ? opt : { value: opt, text: opt })
                        : [];
                    
                    input = `
                        <select class="form-select filter-input" data-field="${field.name}">
                            <option value="">Todos</option>
                            ${options.map(opt => 
                                `<option value="${opt.value || opt}">${opt.text || opt}</option>`
                            ).join('')}
                        </select>
                    `;
                }
            } else {
                input = `
                    <input type="${field.type}" class="form-control filter-input" 
                        data-field="${field.name}" placeholder="Filtrar ${field.label}">
                `;
            }

            col.innerHTML = `
                <label class="form-label">${field.label}</label>
                ${input}
            `;
            filtersContainer.appendChild(col);
        }
    });

    // Agregar evento de filtrado
    document.querySelectorAll('.filter-input').forEach(input => {
        input.addEventListener('input', function() {
            // Obtener el índice de la columna basado en el nombre del campo
            const columnIndex = dataTable.columns().indexes().toArray().find(i => {
                return dataTable.column(i).dataSrc() === this.dataset.field;
            });

            // Si es un select, usar búsqueda exacta
            if (this.tagName.toLowerCase() === 'select') {
                const value = this.value;
                dataTable.column(columnIndex).search(value ? '^' + value + '$' : '', true, false).draw();
                return;
            } else {
                if (columnIndex !== -1) {
                    const value = this.value;
                    dataTable.column(columnIndex).search(value).draw(); 
                    return;
                }
            }
        });
    });
}

// Generar formulario
function generateForm() {
    const form = document.getElementById('dataForm');
    form.innerHTML = '';

    tableConfigs[currentTable].fields.forEach(field => {
        if (field.type === 'hidden'){
            const hiddenInput = document.createElement('input');
            hiddenInput.type = 'hidden';
            hiddenInput.name = field.name;
            form.appendChild(hiddenInput);
            return;
        }

        const div = document.createElement('div');
        div.className = 'mb-3';

        let input = '';
        if (field.type === 'select') {
            // Caso especial para el select de escuelas
            if (field.name === 'escuelaId' && currentTable === '/usuarios') {
                input = `
                    <select class="form-select" name="${field.name}" ${field.required ? 'required' : ''}>
                        <option value="">Seleccione una escuela</option>
                        ${schoolsData.map(school => 
                            `<option value="${school.escuelaId}">${school.nombre}</option>`
                        ).join('')}
                    </select>
                `;
            } else {
                // Para otros selects
                const options = Array.isArray(field.options) 
                    ? field.options.map(opt => typeof opt === 'object' ? opt : { value: opt, text: opt })
                    : [];
                
                input = `
                    <select class="form-select" name="${field.name}" ${field.required ? 'required' : ''}>
                        <option value="">Seleccione</option>
                        ${options.map(opt => 
                            `<option value="${opt.value || opt}">${opt.text || opt}</option>`
                        ).join('')}
                    </select>
                `;
            }
        } else {
            const validationAttrs = [];
            if (field.required) validationAttrs.push('required');
            if (field.maxLength) validationAttrs.push(`maxlength="${field.maxLength}"`);
            if (field.minLength) validationAttrs.push(`minlength="${field.minLength}"`);
            if (field.pattern) validationAttrs.push(`pattern="${field.pattern}"`);
            if (field.min) validationAttrs.push(`min="${field.min}"`);

            input = `
                <input type="${field.type}" class="form-control" name="${field.name}" 
                    ${validationAttrs.join(' ')}>
            `;
        }

        div.innerHTML = `
            <label class="form-label">${field.label}</label>
            ${input}
            ${field.validationMessage ? `<div class="invalid-feedback">${field.validationMessage}</div>` : ''}
        `;

        form.appendChild(div);
    });
}

// Cargar datos
async function loadData() {
    try {
        const response = await fetch(currentTable);
        if (!response.ok) throw new Error('Error al cargar datos');
        
        const data = await response.json();
        
        // Si estamos en la tabla de usuarios, procesamos los datos para mostrar nombres de escuelas
        if (currentTable === '/usuarios') {
            data.forEach(user => {
                const escuela = schoolsData.find(e => e.escuelaId === parseInt(user.escuelaId));
                if (escuela) {
                    user.escuelaName = escuela.nombre;
                }
            });
        }
        
        dataTable.clear();
        dataTable.rows.add(data);
        dataTable.draw();
    } catch (error) {
        showAlert('Error al cargar datos: ' + error.message, 'danger');
    }
}

// Guardar datos
document.getElementById('saveButton').addEventListener('click', async function() {
    const form = document.getElementById('dataForm');
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    try {
        const url = currentEditId ? `${currentTable}/${currentEditId}` : currentTable;
        const method = currentEditId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) throw new Error('Error al guardar datos');

        showAlert('Datos guardados correctamente', 'success');
        $('#createModal').modal('hide');
        loadData();
    } catch (error) {
        showAlert('Error al guardar datos: ' + error.message, 'danger');
    }
});

// Editar registro
async function editRecord(id) {
    currentEditId = id;
    
    try {
        // Obtener el registro específico por ID
        const response = await fetch(`${currentTable}/${id}`);
        if (!response.ok) throw new Error('Error al obtener datos del registro');
        
        const row = await response.json();
        
        const form = document.getElementById('dataForm');
        
        // Limpiar formulario primero
        form.reset();
        
        // Completar los campos del formulario
        tableConfigs[currentTable].fields.forEach(field => {
            const input = form.querySelector(`[name="${field.name}"]`);
            if (input) {
                if (field.name === 'passwordHash' && currentTable === '/usuarios') {
                    // No mostrar la contraseña al editar
                    input.value = '';
                    input.placeholder = 'Dejar en blanco para mantener la actual';
                    input.required = false;
                } else {
                    input.value = row[field.name] || '';
                }
            }
        });

        document.getElementById('modalTitle').textContent = `Editar ${tableConfigs[currentTable].title.slice(0, -1)}`;
        $('#createModal').modal('show');
    } catch (error) {
        showAlert(`Error al editar registro: ${error.message}`, 'danger');
    }
}

// Eliminar registro
async function deleteRecord(id) {
    if (!confirm(`¿Está seguro de eliminar este ${tableConfigs[currentTable].title.slice(0, -1)}? Esta acción no se puede deshacer.`)) return;

    try {
        const response = await fetch(`${currentTable}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error(`Error al eliminar ${tableConfigs[currentTable].title.slice(0, -1)}`);

        showAlert(`${tableConfigs[currentTable].title.slice(0, -1)} eliminado correctamente`, 'success');
        loadData();
    } catch (error) {
        showAlert(`Error al eliminar: ${error.message}`, 'danger');
    }
}

// Mostrar alerta
function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    const container = document.querySelector('.container');
    container.insertBefore(alertDiv, container.firstChild);

    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

// Abrir modal para crear nuevo registro
document.getElementById('newButton').addEventListener('click', function() {
    const form = document.getElementById('dataForm');
    form.reset();
    currentEditId = null;
    document.getElementById('modalTitle').textContent = `Nuevo ${tableConfigs[currentTable].title.slice(0, -1)}`;
    
    // Restablecer campos obligatorios
    if (currentTable === '/usuarios') {
        const passwordField = form.querySelector('[name="passwordHash"]');
        if (passwordField) {
            passwordField.required = true;
            passwordField.placeholder = '';
        }
    }
    
    $('#createModal').modal('show');
});

// Limpiar formulario al cerrar modal
$('#createModal').on('hidden.bs.modal', function() {
    document.getElementById('dataForm').reset();
    currentEditId = null;
    document.getElementById('modalTitle').textContent = `Nuevo ${tableConfigs[currentTable].title.slice(0, -1)}`;
});