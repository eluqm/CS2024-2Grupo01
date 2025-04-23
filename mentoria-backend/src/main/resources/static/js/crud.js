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
            const columnIndex = tableConfigs[currentTable].columns.findIndex(col => col.data === this.dataset.field);
            
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